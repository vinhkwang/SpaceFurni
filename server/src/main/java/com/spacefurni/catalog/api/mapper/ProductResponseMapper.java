package com.spacefurni.catalog.api.mapper;

import com.spacefurni.catalog.api.dto.ProductBadgeResponse;
import com.spacefurni.catalog.api.dto.ProductDetailResponse;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductColorSwatch;
import com.spacefurni.catalog.domain.ProductImage;
import com.spacefurni.catalog.domain.ProductSpecification;
import com.spacefurni.shared.domain.Money;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductResponseMapper {

    public ProductSummaryResponse toSummary(Product product) {
        Money price = product.getPrice();
        Money compareAtPrice = product.getCompareAtPrice();
        return new ProductSummaryResponse(product.getId(), product.getSku(), product.getSlug(), product.getName(),
                product.getCategory().getName(), price.amount(),
                compareAtPrice == null ? null : compareAtPrice.amount(), price.currencyCode(),
                product.getRatingAverage(), product.getReviewCount(), primaryImageUrl(product), toBadge(product),
                product.getPrimaryColorName(), primaryColorHexCode(product));
    }

    public ProductDetailResponse toDetail(Product product, List<Product> relatedProducts, int availableQuantity) {
        Money price = product.getPrice();
        Money compareAtPrice = product.getCompareAtPrice();
        List<ProductDetailResponse.SpecificationEntry> specifications = product.getSpecifications().stream()
                .sorted(Comparator.comparing(ProductSpecification::getDisplayOrder))
                .map(specification -> new ProductDetailResponse.SpecificationEntry(specification.getSpecKey(),
                        specification.getSpecValue()))
                .toList();
        return new ProductDetailResponse(product.getId(), product.getSku(), product.getSlug(), product.getName(),
                product.getCategory().getName(), price.amount(),
                compareAtPrice == null ? null : compareAtPrice.amount(), price.currencyCode(),
                product.getRatingAverage(), product.getReviewCount(), product.getShortDescription(),
                product.getLongDescription(), product.getDimensions(), product.getMaterial(),
                product.getPrimaryColorName(), toBadge(product), orderedImageUrls(product), specifications,
                orderedColorSwatchHexCodes(product), availableQuantity, toStockLabel(availableQuantity),
                relatedProducts.stream().map(this::toSummary).toList());
    }

    private String primaryImageUrl(Product product) {
        return orderedImageUrls(product).stream().findFirst().orElse(null);
    }

    private List<String> orderedImageUrls(Product product) {
        return product.getImages().stream().sorted(Comparator.comparing(ProductImage::getDisplayOrder))
                .map(ProductImage::getUrl).toList();
    }

    private List<String> orderedColorSwatchHexCodes(Product product) {
        return product.getColorSwatches().stream().sorted(Comparator.comparing(ProductColorSwatch::getDisplayOrder))
                .map(ProductColorSwatch::getHexCode).toList();
    }

    private String primaryColorHexCode(Product product) {
        return orderedColorSwatchHexCodes(product).stream().findFirst().orElse(null);
    }

    private String toStockLabel(int availableQuantity) {
        if (availableQuantity <= 0) {
            return "Out of stock";
        }
        if (availableQuantity < 5) {
            return "Only " + availableQuantity + " left";
        }
        return "In stock";
    }

    private ProductBadgeResponse toBadge(Product product) {
        if (product.hasActiveDiscount()) {
            return new ProductBadgeResponse("-" + product.discountPercentage() + "%", "SALE");
        }
        if (Boolean.TRUE.equals(product.getIsNew())) {
            return new ProductBadgeResponse("New", "NEW");
        }
        if (Boolean.TRUE.equals(product.getIsBestseller())) {
            return new ProductBadgeResponse("Bestseller", "BESTSELLER");
        }
        return null;
    }
}
