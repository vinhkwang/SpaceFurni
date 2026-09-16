package com.spacefurni.reviews.application;

import com.spacefurni.reviews.api.dto.AdminReviewRowResponse;
import com.spacefurni.reviews.domain.Review;
import com.spacefurni.reviews.domain.ReviewStatus;
import com.spacefurni.reviews.infrastructure.ReviewRepository;
import com.spacefurni.reviews.infrastructure.ReviewSearchSpecifications;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    public AdminReviewService(ReviewRepository reviewRepository, ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
    }

    @Transactional(readOnly = true)
    public Page<AdminReviewRowResponse> listReviews(ReviewStatus status, UUID productId, Pageable pageable) {
        Specification<Review> specification = Specification.unrestricted();
        if (status != null) {
            specification = specification.and(ReviewSearchSpecifications.hasStatus(status));
        }
        if (productId != null) {
            specification = specification.and(ReviewSearchSpecifications.hasProductId(productId));
        }
        return reviewRepository.findAll(specification, pageable).map(this::toRow);
    }

    @Transactional
    public void updateStatus(UUID reviewId, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        if (status == ReviewStatus.HIDDEN) {
            review.hide();
        } else {
            review.restore();
        }
        reviewRepository.save(review);
        reviewService.recomputeRatingAggregateFor(review.getProductId());
    }

    private AdminReviewRowResponse toRow(Review review) {
        return new AdminReviewRowResponse(review.getId(), review.getProductId(), review.getUserId(),
                review.getRating(), review.getComment(), review.getStatus(), review.getCreatedAt());
    }
}
