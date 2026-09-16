package com.spacefurni.reviews.application;

import com.spacefurni.reviews.domain.Review;
import com.spacefurni.reviews.domain.ReviewStatus;
import com.spacefurni.reviews.infrastructure.ReviewRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    public ReviewQueryService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public Page<Review> findPublishedReviews(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, ReviewStatus.PUBLISHED,
                pageable);
    }

    @Transactional(readOnly = true)
    public Map<Short, Long> findRatingHistogram(UUID productId) {
        Map<Short, Long> histogram = new LinkedHashMap<>();
        for (short star = 5; star >= 1; star--) {
            histogram.put(star, 0L);
        }
        reviewRepository.findRatingHistogramByProductId(productId)
                .forEach(row -> histogram.put(row.getRating(), row.getCount()));
        return histogram;
    }
}
