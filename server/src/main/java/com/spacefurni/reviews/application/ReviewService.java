package com.spacefurni.reviews.application;

import com.spacefurni.catalog.application.ProductRatingAggregateService;
import com.spacefurni.checkout.application.OrderQueryService;
import com.spacefurni.reviews.domain.InvalidReviewException;
import com.spacefurni.reviews.domain.Review;
import com.spacefurni.reviews.infrastructure.ReviewRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewEligibilityService reviewEligibilityService;
    private final ReviewRepository reviewRepository;
    private final OrderQueryService orderQueryService;
    private final ProductRatingAggregateService productRatingAggregateService;

    public ReviewService(ReviewEligibilityService reviewEligibilityService, ReviewRepository reviewRepository,
            OrderQueryService orderQueryService, ProductRatingAggregateService productRatingAggregateService) {
        this.reviewEligibilityService = reviewEligibilityService;
        this.reviewRepository = reviewRepository;
        this.orderQueryService = orderQueryService;
        this.productRatingAggregateService = productRatingAggregateService;
    }

    @Transactional
    public Review submitReview(UUID userId, UUID orderItemId, Short rating, String comment) {
        if (!reviewEligibilityService.isEligibleToReview(userId, orderItemId)) {
            throw new InvalidReviewException("Not eligible to review order item " + orderItemId);
        }
        UUID productId = orderQueryService.findProductIdForOrderItem(orderItemId);
        Review review = reviewRepository.save(new Review(productId, userId, orderItemId, rating, comment));
        recomputeRatingAggregateFor(productId);
        return review;
    }

    void recomputeRatingAggregateFor(UUID productId) {
        ReviewRepository.RatingAggregateRow aggregate = reviewRepository.findRatingAggregateByProductId(productId);
        productRatingAggregateService.recomputeRatingAggregate(productId, aggregate.getAverage(),
                (int) aggregate.getCount());
    }
}
