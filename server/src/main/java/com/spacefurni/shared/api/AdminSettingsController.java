package com.spacefurni.shared.api;

import com.spacefurni.shared.api.dto.PlatformSettingsResponse;
import com.spacefurni.shared.api.dto.UpdatePlatformSettingsRequest;
import com.spacefurni.shared.application.PlatformSettingsService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final PlatformSettingsService platformSettingsService;

    public AdminSettingsController(PlatformSettingsService platformSettingsService) {
        this.platformSettingsService = platformSettingsService;
    }

    @GetMapping
    public ApiResponse<PlatformSettingsResponse> getSettings() {
        return ApiResponse.success(toResponse(platformSettingsService.getSettings()));
    }

    @PatchMapping
    public ApiResponse<Void> updateSettings(@Valid @RequestBody UpdatePlatformSettingsRequest request) {
        platformSettingsService.updateSettings(request.freeDeliveryThresholdAmount(),
                request.standardDeliveryFeeAmount(), request.nextDayDeliveryFeeAmount(),
                request.lowStockThresholdUnits());
        return ApiResponse.success(null);
    }

    private PlatformSettingsResponse toResponse(PlatformSettingsService.PlatformSettingsSnapshot settings) {
        return new PlatformSettingsResponse(settings.freeDeliveryThreshold().amount(),
                settings.standardDeliveryFee().amount(), settings.nextDayDeliveryFee().amount(),
                settings.lowStockThresholdUnits());
    }
}
