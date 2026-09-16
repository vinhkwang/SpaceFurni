package com.spacefurni.reviews.application;

import com.spacefurni.checkout.application.OrderQueryService;
import com.spacefurni.reviews.infrastructure.ReviewRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewEligibilityService {

    private final OrderQueryService orderQueryService;
    private final ReviewRepository reviewRepository;

    public ReviewEligibilityService(OrderQueryService orderQueryService, ReviewRepository reviewRepository) {
        this.orderQueryService = orderQueryService;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public boolean isEligibleToReview(UUID userId, UUID orderItemId) {
        return orderQueryService.isOrderItemDeliveredForUser(userId, orderItemId)
                && !reviewRepository.existsByOrderItemId(orderItemId);
    }
}
