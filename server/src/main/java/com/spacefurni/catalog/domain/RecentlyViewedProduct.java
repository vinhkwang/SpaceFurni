package com.spacefurni.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recently_viewed_products")
public class RecentlyViewedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "guest_token")
    private UUID guestToken;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "viewed_at", nullable = false)
    private Instant viewedAt;

    protected RecentlyViewedProduct() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getGuestToken() {
        return guestToken;
    }

    public UUID getProductId() {
        return productId;
    }

    public Instant getViewedAt() {
        return viewedAt;
    }
}
