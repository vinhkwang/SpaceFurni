package com.spacefurni.shared.application;

import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.domain.PlatformSettings;
import com.spacefurni.shared.infrastructure.PlatformSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformSettingsService {

    private final PlatformSettingsRepository platformSettingsRepository;

    public PlatformSettingsService(PlatformSettingsRepository platformSettingsRepository) {
        this.platformSettingsRepository = platformSettingsRepository;
    }

    @Transactional(readOnly = true)
    public PlatformSettingsSnapshot getSettings() {
        PlatformSettings settings = platformSettingsRepository.findSingleton();
        return new PlatformSettingsSnapshot(Money.ofVnd(settings.getFreeDeliveryThresholdAmount()),
                Money.ofVnd(settings.getStandardDeliveryFeeAmount()),
                Money.ofVnd(settings.getNextDayDeliveryFeeAmount()), settings.getLowStockThresholdUnits());
    }

    @Transactional
    public void updateSettings(long freeDeliveryThresholdAmount, long standardDeliveryFeeAmount,
            long nextDayDeliveryFeeAmount, int lowStockThresholdUnits) {
        PlatformSettings settings = platformSettingsRepository.findSingleton();
        settings.updateThresholds(freeDeliveryThresholdAmount, standardDeliveryFeeAmount,
                nextDayDeliveryFeeAmount, lowStockThresholdUnits);
        platformSettingsRepository.save(settings);
    }

    public record PlatformSettingsSnapshot(Money freeDeliveryThreshold, Money standardDeliveryFee,
            Money nextDayDeliveryFee, int lowStockThresholdUnits) {
    }
}
