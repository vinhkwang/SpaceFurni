package com.spacefurni.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "platform_settings")
@EntityListeners(AuditingEntityListener.class)
public class PlatformSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "free_delivery_threshold_amount", nullable = false)
    private Long freeDeliveryThresholdAmount;

    @Column(name = "standard_delivery_fee_amount", nullable = false)
    private Long standardDeliveryFeeAmount;

    @Column(name = "next_day_delivery_fee_amount", nullable = false)
    private Long nextDayDeliveryFeeAmount;

    @Column(name = "low_stock_threshold_units", nullable = false)
    private Integer lowStockThresholdUnits;

    @Version
    private Long version;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlatformSettings() {
    }

    public void updateThresholds(long freeDeliveryThresholdAmount, long standardDeliveryFeeAmount,
            long nextDayDeliveryFeeAmount, int lowStockThresholdUnits) {
        this.freeDeliveryThresholdAmount = freeDeliveryThresholdAmount;
        this.standardDeliveryFeeAmount = standardDeliveryFeeAmount;
        this.nextDayDeliveryFeeAmount = nextDayDeliveryFeeAmount;
        this.lowStockThresholdUnits = lowStockThresholdUnits;
    }

    public UUID getId() {
        return id;
    }

    public long getFreeDeliveryThresholdAmount() {
        return freeDeliveryThresholdAmount;
    }

    public long getStandardDeliveryFeeAmount() {
        return standardDeliveryFeeAmount;
    }

    public long getNextDayDeliveryFeeAmount() {
        return nextDayDeliveryFeeAmount;
    }

    public int getLowStockThresholdUnits() {
        return lowStockThresholdUnits;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
