package com.spacefurni.shared.infrastructure;

import com.spacefurni.shared.domain.PlatformSettings;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, UUID> {

    default PlatformSettings findSingleton() {
        List<PlatformSettings> allSettings = findAll();
        if (allSettings.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one platform_settings row but found " + allSettings.size());
        }
        return allSettings.get(0);
    }
}
