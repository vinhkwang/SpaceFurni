package com.spacefurni.reviews.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "order_item_id", nullable = false, unique = true)
    private UUID orderItemId;

    @Column(nullable = false)
    private Short rating;

    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Review() {
    }

    public Review(UUID productId, UUID userId, UUID orderItemId, Short rating, String comment) {
        this.productId = productId;
        this.userId = userId;
        this.orderItemId = orderItemId;
        this.rating = rating;
        this.comment = comment;
        this.status = ReviewStatus.PUBLISHED;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getOrderItemId() {
        return orderItemId;
    }

    public Short getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
