package com.spacefurni.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.inventory.api.dto.StockReservationLine;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.inventory.domain.InsufficientStockException;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.datasource.hikari.maximum-pool-size=60")
class InventoryConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void exactlyOneOfTwentyConcurrentReservationsSucceedsWhenStockIsOne() throws InterruptedException {
        UUID productId = seedProductWithStock(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger insufficientStockCount = new AtomicInteger();

        runConcurrently(20, () -> {
            try {
                inventoryService.reserveStockForOrderLines(List.of(new StockReservationLine(productId, 1)));
                successCount.incrementAndGet();
            } catch (InsufficientStockException exception) {
                insufficientStockCount.incrementAndGet();
            }
        });

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(insufficientStockCount.get()).isEqualTo(19);
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(0);
    }

    @Test
    void allFiftyConcurrentSingleUnitReservationsSucceedWhenStockIsFifty() throws InterruptedException {
        UUID productId = seedProductWithStock(50);
        AtomicInteger successCount = new AtomicInteger();

        runConcurrently(50, () -> {
            inventoryService.reserveStockForOrderLines(List.of(new StockReservationLine(productId, 1)));
            successCount.incrementAndGet();
        });

        assertThat(successCount.get()).isEqualTo(50);
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(0);
    }

    @Test
    void reservingTwoProductsInOppositeOrderAcrossTwoThreadsDoesNotDeadlock() throws InterruptedException {
        UUID productA = seedProductWithStock(10);
        UUID productB = seedProductWithStock(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(reservationTask(startLatch, doneLatch,
                List.of(new StockReservationLine(productA, 1), new StockReservationLine(productB, 1))));
        executor.submit(reservationTask(startLatch, doneLatch,
                List.of(new StockReservationLine(productB, 1), new StockReservationLine(productA, 1))));

        startLatch.countDown();
        boolean completedWithoutDeadlock = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completedWithoutDeadlock).as("both threads finished without deadlocking").isTrue();
        assertThat(inventoryItemRepository.findById(productA).orElseThrow().getQuantityOnHand()).isEqualTo(8);
        assertThat(inventoryItemRepository.findById(productB).orElseThrow().getQuantityOnHand()).isEqualTo(8);
    }

    private Runnable reservationTask(CountDownLatch startLatch, CountDownLatch doneLatch,
            List<StockReservationLine> lines) {
        return () -> {
            try {
                startLatch.await();
                inventoryService.reserveStockForOrderLines(lines);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };
    }

    private void runConcurrently(int attempts, Runnable reservationAttempt) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(attempts);
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        for (int i = 0; i < attempts; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reservationAttempt.run();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        assertThat(doneLatch.await(15, TimeUnit.SECONDS)).as("all attempts finished within timeout").isTrue();
        executor.shutdown();
    }

    private UUID seedProductWithStock(int quantityOnHand) {
        Category department = categoryRepository
                .save(new Category(null, "Living room", "living-room-" + UUID.randomUUID(), null, 1));
        Category subCategory = categoryRepository
                .save(new Category(department, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Oslo Sofa", "oslo-sofa-" + UUID.randomUUID(),
                subCategory, Money.ofVnd(5_000_000L), null, ProductStatus.DRAFT, "short", "long", "200x90x80cm",
                "Fabric", "Beige", new BigDecimal("4.5"), 12, true, false);
        productRepository.saveAndFlush(product);
        inventoryItemRepository.saveAndFlush(new InventoryItem(product.getId(), quantityOnHand, 0));
        return product.getId();
    }
}
