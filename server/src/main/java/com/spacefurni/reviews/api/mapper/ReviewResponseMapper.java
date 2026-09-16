package com.spacefurni.reviews.api.mapper;

import com.spacefurni.reviews.api.dto.RatingHistogramResponse;
import com.spacefurni.reviews.api.dto.ReviewResponse;
import com.spacefurni.reviews.domain.Review;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ReviewResponseMapper {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(review.getId(), review.getProductId(), review.getUserId(), review.getRating(),
                review.getComment(), review.getCreatedAt());
    }

    public RatingHistogramResponse toHistogramResponse(Map<Short, Long> histogram) {
        return new RatingHistogramResponse(histogram.entrySet().stream()
                .map(entry -> new RatingHistogramResponse.StarRatingCount(entry.getKey(), entry.getValue()))
                .toList());
    }
}
