package com.spacefurni.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "inventory_items")
@EntityListeners(AuditingEntityListener.class)
public class InventoryItem {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved;

    @Version
    private Long version;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InventoryItem() {
    }

    public InventoryItem(UUID productId, Integer quantityOnHand, Integer quantityReserved) {
        this.productId = productId;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = quantityReserved;
    }

    public int availableQuantity() {
        return quantityOnHand;
    }

    public UUID getProductId() {
        return productId;
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public Integer getQuantityReserved() {
        return quantityReserved;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
