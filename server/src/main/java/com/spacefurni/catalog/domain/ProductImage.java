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
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String url;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    protected ProductImage() {
    }

    public ProductImage(Product product, String url, Integer displayOrder) {
        this.product = product;
        this.url = url;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getUrl() {
        return url;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
