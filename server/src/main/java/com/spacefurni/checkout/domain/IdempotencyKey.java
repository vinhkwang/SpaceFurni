package com.spacefurni.checkout.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    private String key;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "request_fingerprint", nullable = false)
    private String requestFingerprint;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(String key, UUID userId, String requestFingerprint) {
        this.key = key;
        this.userId = userId;
        this.requestFingerprint = requestFingerprint;
        this.createdAt = Instant.now();
    }

    public void assignOrder(UUID orderId) {
        this.orderId = orderId;
    }

    public String getKey() {
        return key;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRequestFingerprint() {
        return requestFingerprint;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
