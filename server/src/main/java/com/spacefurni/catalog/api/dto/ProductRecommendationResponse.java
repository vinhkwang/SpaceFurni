package com.spacefurni.catalog.api.dto;

import java.util.List;

public record ProductRecommendationResponse(List<ProductSummaryResponse> products) {
}
