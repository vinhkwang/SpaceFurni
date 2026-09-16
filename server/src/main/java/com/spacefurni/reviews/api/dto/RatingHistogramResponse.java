package com.spacefurni.reviews.api.dto;

import java.util.List;

public record RatingHistogramResponse(List<StarRatingCount> counts) {

    public record StarRatingCount(short stars, long count) {
    }
}
