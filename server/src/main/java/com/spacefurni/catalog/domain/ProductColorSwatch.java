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
@Table(name = "product_color_swatches")
public class ProductColorSwatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "hex_code", nullable = false)
    private String hexCode;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    protected ProductColorSwatch() {
    }

    public ProductColorSwatch(Product product, String hexCode, Integer displayOrder) {
        this.product = product;
        this.hexCode = hexCode;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getHexCode() {
        return hexCode;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
