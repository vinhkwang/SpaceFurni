package com.spacefurni.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "product_specifications")
public class ProductSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "spec_key", nullable = false)
    private String specKey;

    @Column(name = "spec_value", nullable = false)
    private String specValue;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    protected ProductSpecification() {
    }

    public ProductSpecification(Product product, String specKey, String specValue, Integer displayOrder) {
        this.product = product;
        this.specKey = specKey;
        this.specValue = specValue;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getSpecKey() {
        return specKey;
    }

    public String getSpecValue() {
        return specValue;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
