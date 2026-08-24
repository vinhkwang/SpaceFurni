package com.spacefurni.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class ProductPersistenceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsAndReloadsProductWithoutCompareAtPrice() {
        Category sofa = categoryRepository.save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Oslo Sofa", "oslo-sofa-" + UUID.randomUUID(),
                sofa, Money.ofVnd(5_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "200x90x80cm",
                "Fabric", "Beige", new BigDecimal("4.5"), 12, true, false);

        UUID id = entityManager.persistAndFlush(product).getId();
        entityManager.clear();

        Product reloaded = entityManager.find(Product.class, id);

        assertThat(reloaded.getPrice().amount()).isEqualTo(5_000_000L);
        assertThat(reloaded.getPrice().currencyCode()).isEqualTo("VND");
        assertThat(reloaded.getCompareAtPrice()).isNull();
        assertThat(reloaded.hasActiveDiscount()).isFalse();
        assertThat(reloaded.discountPercentage()).isZero();
    }

    @Test
    void persistsAndReloadsProductWithCompareAtPriceAndComputesDiscount() {
        Category chair = categoryRepository.save(new Category(null, "Chair", "chair-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Copenhagen Chair",
                "copenhagen-chair-" + UUID.randomUUID(), chair, Money.ofVnd(800_000L), Money.ofVnd(1_000_000L),
                ProductStatus.PUBLISHED, "short", "long", "60x60x90cm", "Oak", "Walnut", new BigDecimal("4.8"), 30,
                false, true);

        UUID id = entityManager.persistAndFlush(product).getId();
        entityManager.clear();

        Product reloaded = entityManager.find(Product.class, id);

        assertThat(reloaded.getCompareAtPrice().amount()).isEqualTo(1_000_000L);
        assertThat(reloaded.getCompareAtPrice().currencyCode()).isEqualTo("VND");
        assertThat(reloaded.hasActiveDiscount()).isTrue();
        assertThat(reloaded.discountPercentage()).isEqualTo(20);
    }

    @Test
    void cascadePersistsChildEntitiesOrderedByDisplayOrder() {
        Category bed = categoryRepository.save(new Category(null, "Bed", "bed-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Nordic Bed", "nordic-bed-" + UUID.randomUUID(),
                bed, Money.ofVnd(12_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "160x200cm", "Oak",
                "Natural", new BigDecimal("4.6"), 8, true, false);
        product.addImage("https://example.com/b.jpg", 2);
        product.addImage("https://example.com/a.jpg", 1);
        product.addSpecification("Material", "Solid oak", 1);
        product.addColorSwatch("#8B5E3C", 1);

        UUID id = entityManager.persistAndFlush(product).getId();
        entityManager.clear();

        Product reloaded = entityManager.find(Product.class, id);

        assertThat(reloaded.getImages()).extracting(ProductImage::getUrl)
                .containsExactly("https://example.com/a.jpg", "https://example.com/b.jpg");
        assertThat(reloaded.getSpecifications()).hasSize(1);
        assertThat(reloaded.getColorSwatches()).extracting(ProductColorSwatch::getHexCode).containsExactly("#8B5E3C");
    }

    @Test
    void orphanRemovalDeletesChildRowWhenRemovedFromCollection() {
        Category desk = categoryRepository.save(new Category(null, "Desk", "desk-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Study Desk", "study-desk-" + UUID.randomUUID(),
                desk, Money.ofVnd(3_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "120x60x75cm", "Pine",
                "White", new BigDecimal("4.2"), 5, false, false);
        product.addImage("https://example.com/desk.jpg", 1);
        entityManager.persistAndFlush(product);

        ProductImage image = product.getImages().get(0);
        product.removeImage(image);
        entityManager.persistAndFlush(product);
        entityManager.clear();

        assertThat(entityManager.find(ProductImage.class, image.getId())).isNull();
    }
}
