package com.spacefurni.reviews.api.dto;

import com.spacefurni.reviews.domain.ReviewStatus;
import java.time.Instant;
import java.util.UUID;

public record AdminReviewRowResponse(UUID id, UUID productId, UUID userId, Short rating, String comment,
        ReviewStatus status, Instant createdAt) {
}
