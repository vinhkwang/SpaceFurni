package com.spacefurni.reviews.api;

import com.spacefurni.reviews.api.dto.AdminReviewRowResponse;
import com.spacefurni.reviews.application.AdminReviewService;
import com.spacefurni.reviews.domain.ReviewStatus;
import com.spacefurni.shared.api.ApiResponse;
import com.spacefurni.shared.api.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminReviewService adminReviewService;

    public AdminReviewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminReviewRowResponse>> listReviews(
            @RequestParam(required = false) ReviewStatus status, @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        Page<AdminReviewRowResponse> result = adminReviewService.listReviews(status, productId,
                PageRequest.of(resolvePage(page), resolvePageSize(size)));
        return ApiResponse.success(new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID id, @RequestParam ReviewStatus status) {
        adminReviewService.updateStatus(id, status);
        return ApiResponse.success(null);
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
