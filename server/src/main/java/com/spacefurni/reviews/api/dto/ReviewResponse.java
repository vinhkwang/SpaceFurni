package com.spacefurni.reviews.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(UUID id, UUID productId, UUID userId, Short rating, String comment,
        Instant createdAt) {
}
