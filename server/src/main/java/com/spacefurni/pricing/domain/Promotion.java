package com.spacefurni.pricing.domain;

import com.spacefurni.shared.domain.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionType type;

    @Column(nullable = false)
    private Long value;

    @Column(name = "minimum_subtotal_amount")
    private Long minimumSubtotalAmount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    protected Promotion() {
    }

    public Promotion(String code, PromotionType type, Long value, Long minimumSubtotalAmount, Boolean isActive,
            Instant startsAt, Instant endsAt) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.minimumSubtotalAmount = minimumSubtotalAmount;
        this.isActive = isActive;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public boolean isRedeemableAt(Instant instant, Money subtotal) {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        if (startsAt != null && instant.isBefore(startsAt)) {
            return false;
        }
        if (endsAt != null && instant.isAfter(endsAt)) {
            return false;
        }
        return minimumSubtotalAmount == null || subtotal.amount() >= minimumSubtotalAmount;
    }

    public String getCode() {
        return code;
    }

    public PromotionType getType() {
        return type;
    }

    public Long getValue() {
        return value;
    }

    public Long getMinimumSubtotalAmount() {
        return minimumSubtotalAmount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }
}
