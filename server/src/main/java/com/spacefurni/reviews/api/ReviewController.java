package com.spacefurni.reviews.api;

import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.reviews.api.dto.RatingHistogramResponse;
import com.spacefurni.reviews.api.dto.ReviewRequest;
import com.spacefurni.reviews.api.dto.ReviewResponse;
import com.spacefurni.reviews.api.mapper.ReviewResponseMapper;
import com.spacefurni.reviews.application.ReviewQueryService;
import com.spacefurni.reviews.application.ReviewService;
import com.spacefurni.reviews.domain.Review;
import com.spacefurni.shared.api.ApiResponse;
import com.spacefurni.shared.api.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ReviewController {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 48;

    private final ReviewService reviewService;
    private final ReviewQueryService reviewQueryService;
    private final ReviewResponseMapper reviewResponseMapper;
    private final CurrentUserQueryService currentUserQueryService;

    public ReviewController(ReviewService reviewService, ReviewQueryService reviewQueryService,
            ReviewResponseMapper reviewResponseMapper, CurrentUserQueryService currentUserQueryService) {
        this.reviewService = reviewService;
        this.reviewQueryService = reviewQueryService;
        this.reviewResponseMapper = reviewResponseMapper;
        this.currentUserQueryService = currentUserQueryService;
    }

    @PostMapping
    public ApiResponse<ReviewResponse> submitReview(@AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID productId, @Valid @RequestBody ReviewRequest request) {
        Review review = reviewService.submitReview(resolveUserId(principal), request.orderItemId(),
                request.rating(), request.comment());
        return ApiResponse.success(reviewResponseMapper.toResponse(review));
    }

    @GetMapping
    public ApiResponse<PageResponse<ReviewResponse>> listReviews(@PathVariable UUID productId,
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        Page<Review> result = reviewQueryService.findPublishedReviews(productId,
                PageRequest.of(resolvePage(page), resolvePageSize(size)));
        return ApiResponse.success(new PageResponse<>(
                result.getContent().stream().map(reviewResponseMapper::toResponse).toList(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages()));
    }

    @GetMapping("/histogram")
    public ApiResponse<RatingHistogramResponse> ratingHistogram(@PathVariable UUID productId) {
        return ApiResponse
                .success(reviewResponseMapper.toHistogramResponse(reviewQueryService.findRatingHistogram(productId)));
    }

    private UUID resolveUserId(UserDetails principal) {
        return currentUserQueryService.getByEmail(principal.getUsername()).getId();
    }

    private int resolvePage(Integer requestedPage) {
        return requestedPage == null ? 0 : requestedPage;
    }

    private int resolvePageSize(Integer requestedSize) {
        if (requestedSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.max(1, Math.min(requestedSize, MAX_PAGE_SIZE));
    }
}
