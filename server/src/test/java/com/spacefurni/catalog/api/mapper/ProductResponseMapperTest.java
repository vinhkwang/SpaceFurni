package com.spacefurni.catalog.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.api.dto.ProductDetailResponse;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.shared.domain.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductResponseMapperTest {

    private final ProductResponseMapper mapper = new ProductResponseMapper();

    @Test
    void toSummaryMapsRawAmountsAndSaleBadgeTakesPriorityOverNewAndBestseller() {
        Category category = new Category(null, "Sofa", "sofa", null, 1);
        Product product = new Product("SKU-1", "Oslo Sofa", "oslo-sofa", category, Money.ofVnd(800_000L),
                Money.ofVnd(1_000_000L), ProductStatus.PUBLISHED, "short", "long", "200x90x80cm", "Fabric", "Beige",
                new BigDecimal("4.5"), 12, true, true);
        product.addImage("https://example.com/b.jpg", 2);
        product.addImage("https://example.com/a.jpg", 1);

        ProductSummaryResponse summary = mapper.toSummary(product);

        assertThat(summary.priceAmount()).isEqualTo(800_000L);
        assertThat(summary.compareAtPriceAmount()).isEqualTo(1_000_000L);
        assertThat(summary.currencyCode()).isEqualTo("VND");
        assertThat(summary.categoryName()).isEqualTo("Sofa");
        assertThat(summary.primaryImageUrl()).isEqualTo("https://example.com/a.jpg");
        assertThat(summary.badge().label()).isEqualTo("-20%");
        assertThat(summary.badge().variant()).isEqualTo("SALE");
    }

    @Test
    void toSummaryShowsNewBadgeWhenNoActiveDiscount() {
        Category category = new Category(null, "Chair", "chair", null, 1);
        Product product = new Product("SKU-2", "Copenhagen Chair", "copenhagen-chair", category,
                Money.ofVnd(800_000L), null, ProductStatus.PUBLISHED, "short", "long", "60x60x90cm", "Oak", "Walnut",
                new BigDecimal("4.8"), 30, true, true);

        ProductSummaryResponse summary = mapper.toSummary(product);

        assertThat(summary.badge().label()).isEqualTo("New");
        assertThat(summary.badge().variant()).isEqualTo("NEW");
    }

    @Test
    void toSummaryShowsBestsellerBadgeWhenNeitherSaleNorNew() {
        Category category = new Category(null, "Table", "table", null, 1);
        Product product = new Product("SKU-3", "Birch Table", "birch-table", category, Money.ofVnd(2_000_000L),
                null, ProductStatus.PUBLISHED, "short", "long", "120x60cm", "Birch", "Natural",
                new BigDecimal("4.0"), 3, false, true);

        ProductSummaryResponse summary = mapper.toSummary(product);

        assertThat(summary.badge().label()).isEqualTo("Bestseller");
        assertThat(summary.badge().variant()).isEqualTo("BESTSELLER");
    }

    @Test
    void toSummaryHasNoBadgeWhenNoRuleApplies() {
        Category category = new Category(null, "Desk", "desk", null, 1);
        Product product = new Product("SKU-4", "Study Desk", "study-desk", category, Money.ofVnd(3_000_000L), null,
                ProductStatus.PUBLISHED, "short", "long", "120x60x75cm", "Pine", "White", new BigDecimal("4.2"), 5,
                false, false);

        ProductSummaryResponse summary = mapper.toSummary(product);

        assertThat(summary.badge()).isNull();
        assertThat(summary.primaryImageUrl()).isNull();
    }

    @Test
    void toDetailMapsChildCollectionsAndRelatedProducts() {
        Category category = new Category(null, "Bed", "bed", null, 1);
        Product product = new Product("SKU-5", "Nordic Bed", "nordic-bed", category, Money.ofVnd(12_000_000L), null,
                ProductStatus.PUBLISHED, "short", "long", "160x200cm", "Oak", "Natural", new BigDecimal("4.6"), 8,
                false, false);
        product.addImage("https://example.com/bed.jpg", 1);
        product.addSpecification("Material", "Solid oak", 1);
        product.addColorSwatch("#8B5E3C", 1);
        Product related = new Product("SKU-6", "Spindle Bedside Table", "spindle-bedside-table", category,
                Money.ofVnd(4_300_000L), null, ProductStatus.PUBLISHED, "short", "long", "52x40x55cm", "Oak",
                "Natural", new BigDecimal("4.8"), 62, true, true);

        ProductDetailResponse detail = mapper.toDetail(product, List.of(related));

        assertThat(detail.imageUrls()).containsExactly("https://example.com/bed.jpg");
        assertThat(detail.specifications()).containsExactly(
                new ProductDetailResponse.SpecificationEntry("Material", "Solid oak"));
        assertThat(detail.colorSwatchHexCodes()).containsExactly("#8B5E3C");
        assertThat(detail.relatedProducts()).hasSize(1);
        assertThat(detail.relatedProducts().get(0).slug()).isEqualTo("spindle-bedside-table");
    }
}
