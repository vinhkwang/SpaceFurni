package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.CustomerTier;
import java.time.Instant;
import java.util.UUID;

public record AdminCustomerRowResponse(UUID id, String fullName, String email, String district, long orderCount,
        long lifetimeValueAmount, String currencyCode, Instant firstOrderAt, CustomerTier tier) {
}
