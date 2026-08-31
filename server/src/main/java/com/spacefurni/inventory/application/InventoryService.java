package com.spacefurni.inventory.application;

import com.spacefurni.inventory.api.dto.StockReservationLine;
import com.spacefurni.inventory.domain.InsufficientStockException;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Transactional
    public void reserveStockForOrderLines(List<StockReservationLine> lines) {
        for (StockReservationLine line : sortLinesByProductIdForDeterministicLockOrdering(lines)) {
            int rowsAffected = inventoryItemRepository
                    .decrementQuantityOnHandIfSufficient(line.productId(), line.quantity());
            if (rowsAffected == 0) {
                throw new InsufficientStockException(line.productId(), line.quantity(),
                        availableQuantityOf(line.productId()));
            }
        }
    }

    @Transactional
    public void releaseStockForOrderLines(List<StockReservationLine> lines) {
        for (StockReservationLine line : lines) {
            inventoryItemRepository.incrementQuantityOnHand(line.productId(), line.quantity());
        }
    }

    @Transactional
    public void provisionInitialStock(UUID productId, int quantityOnHand) {
        inventoryItemRepository.save(new InventoryItem(productId, quantityOnHand, 0));
    }

    @Transactional
    public void adjustQuantityOnHand(UUID productId, int delta) {
        int rowsAffected = inventoryItemRepository.adjustQuantityOnHandIfSufficient(productId, delta);
        if (rowsAffected == 0) {
            if (delta < 0) {
                throw new InsufficientStockException(productId, -delta, availableQuantityOf(productId));
            }
            throw new ResourceNotFoundException("Inventory item not found: " + productId);
        }
    }

    @Transactional(readOnly = true)
    public Map<UUID, Integer> findAvailableQuantities(List<UUID> productIds) {
        return inventoryItemRepository.findAllByProductIdInOrderByProductIdAsc(productIds).stream()
                .collect(Collectors.toMap(InventoryItem::getProductId, InventoryItem::availableQuantity));
    }

    private int availableQuantityOf(UUID productId) {
        return findAvailableQuantities(List.of(productId)).getOrDefault(productId, 0);
    }

    private List<StockReservationLine> sortLinesByProductIdForDeterministicLockOrdering(
            List<StockReservationLine> lines) {
        return lines.stream().sorted(Comparator.comparing(StockReservationLine::productId)).toList();
    }
}
