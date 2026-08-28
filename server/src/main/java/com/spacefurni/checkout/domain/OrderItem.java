package com.spacefurni.checkout.domain;

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
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(name = "sku_snapshot", nullable = false)
    private String skuSnapshot;

    @Column(name = "unit_price_amount", nullable = false)
    private Long unitPriceAmount;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "line_total_amount", nullable = false)
    private Long lineTotalAmount;

    protected OrderItem() {
    }

    public OrderItem(UUID productId, String productNameSnapshot, String skuSnapshot, Long unitPriceAmount,
            Integer quantity, Long lineTotalAmount) {
        this.productId = productId;
        this.productNameSnapshot = productNameSnapshot;
        this.skuSnapshot = skuSnapshot;
        this.unitPriceAmount = unitPriceAmount;
        this.quantity = quantity;
        this.lineTotalAmount = lineTotalAmount;
    }

    void assignToOrder(Order order) {
        this.order = order;
    }

    public UUID getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public String getSkuSnapshot() {
        return skuSnapshot;
    }

    public Long getUnitPriceAmount() {
        return unitPriceAmount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Long getLineTotalAmount() {
        return lineTotalAmount;
    }
}
