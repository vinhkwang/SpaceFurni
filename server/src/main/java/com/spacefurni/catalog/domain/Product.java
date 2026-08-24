package com.spacefurni.catalog.domain;

import com.spacefurni.shared.domain.AuditableEntity;
import com.spacefurni.shared.domain.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "currency_code", nullable = false))
    })
    private Money price;

    @Column(name = "compare_at_price_amount")
    private Long compareAtPriceAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "long_description")
    private String longDescription;

    private String dimensions;

    private String material;

    @Column(name = "primary_color_name")
    private String primaryColorName;

    @Column(name = "rating_average")
    private BigDecimal ratingAverage;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Column(name = "is_new", nullable = false)
    private Boolean isNew;

    @Column(name = "is_bestseller", nullable = false)
    private Boolean isBestseller;

    @Version
    private Long version;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductSpecification> specifications = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductColorSwatch> colorSwatches = new ArrayList<>();

    protected Product() {
    }

    public Product(String sku, String name, String slug, Category category, Money price, Money compareAtPrice,
            ProductStatus status, String shortDescription, String longDescription, String dimensions,
            String material, String primaryColorName, BigDecimal ratingAverage, Integer reviewCount, Boolean isNew,
            Boolean isBestseller) {
        this.sku = sku;
        this.name = name;
        this.slug = slug;
        this.category = category;
        this.price = price;
        this.compareAtPriceAmount = compareAtPrice == null ? null : compareAtPrice.amount();
        this.status = status;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.dimensions = dimensions;
        this.material = material;
        this.primaryColorName = primaryColorName;
        this.ratingAverage = ratingAverage;
        this.reviewCount = reviewCount;
        this.isNew = isNew;
        this.isBestseller = isBestseller;
    }

    public boolean hasActiveDiscount() {
        return compareAtPriceAmount != null && compareAtPriceAmount > price.amount();
    }

    public int discountPercentage() {
        if (!hasActiveDiscount()) {
            return 0;
        }
        return (int) ((compareAtPriceAmount - price.amount()) * 100 / compareAtPriceAmount);
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public Category getCategory() {
        return category;
    }

    public Money getPrice() {
        return price;
    }

    public Money getCompareAtPrice() {
        return compareAtPriceAmount == null ? null : new Money(compareAtPriceAmount, price.currencyCode());
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getDimensions() {
        return dimensions;
    }

    public String getMaterial() {
        return material;
    }

    public String getPrimaryColorName() {
        return primaryColorName;
    }

    public BigDecimal getRatingAverage() {
        return ratingAverage;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public Boolean getIsBestseller() {
        return isBestseller;
    }

    public Long getVersion() {
        return version;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public List<ProductSpecification> getSpecifications() {
        return specifications;
    }

    public List<ProductColorSwatch> getColorSwatches() {
        return colorSwatches;
    }

    public void addImage(String url, Integer displayOrder) {
        images.add(new ProductImage(this, url, displayOrder));
    }

    public void addSpecification(String specKey, String specValue, Integer displayOrder) {
        specifications.add(new ProductSpecification(this, specKey, specValue, displayOrder));
    }

    public void addColorSwatch(String hexCode, Integer displayOrder) {
        colorSwatches.add(new ProductColorSwatch(this, hexCode, displayOrder));
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
    }

    public void removeSpecification(ProductSpecification specification) {
        specifications.remove(specification);
    }

    public void removeColorSwatch(ProductColorSwatch colorSwatch) {
        colorSwatches.remove(colorSwatch);
    }
}
