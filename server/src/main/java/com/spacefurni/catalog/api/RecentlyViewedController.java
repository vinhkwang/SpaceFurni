package com.spacefurni.catalog.api;

import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.api.dto.RecordRecentlyViewedResponse;
import com.spacefurni.catalog.application.RecentlyViewedQueryService;
import com.spacefurni.catalog.application.RecentlyViewedService;
import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.shared.api.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recently-viewed")
public class RecentlyViewedController {

    private static final String GUEST_TOKEN_HEADER = "X-Guest-Token";

    private final RecentlyViewedService recentlyViewedService;
    private final RecentlyViewedQueryService recentlyViewedQueryService;
    private final CurrentUserQueryService currentUserQueryService;

    public RecentlyViewedController(RecentlyViewedService recentlyViewedService,
            RecentlyViewedQueryService recentlyViewedQueryService,
            CurrentUserQueryService currentUserQueryService) {
        this.recentlyViewedService = recentlyViewedService;
        this.recentlyViewedQueryService = recentlyViewedQueryService;
        this.currentUserQueryService = currentUserQueryService;
    }

    @PostMapping("/{productId}")
    public ApiResponse<RecordRecentlyViewedResponse> recordView(@AuthenticationPrincipal UserDetails principal,
            @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String guestTokenHeader,
            @PathVariable UUID productId) {
        UUID userId = resolveUserId(principal);
        UUID guestToken = userId != null ? null
                : guestTokenHeader != null ? UUID.fromString(guestTokenHeader) : UUID.randomUUID();
        recentlyViewedService.recordView(userId, guestToken, productId);
        return ApiResponse.success(new RecordRecentlyViewedResponse(guestToken));
    }

    @GetMapping
    public ApiResponse<List<ProductSummaryResponse>> recentlyViewed(@AuthenticationPrincipal UserDetails principal,
            @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String guestTokenHeader,
            @RequestParam(required = false) UUID excludeProductId) {
        UUID userId = resolveUserId(principal);
        UUID guestToken = userId == null && guestTokenHeader != null ? UUID.fromString(guestTokenHeader) : null;
        if (userId == null && guestToken == null) {
            return ApiResponse.success(List.of());
        }
        return ApiResponse
                .success(recentlyViewedQueryService.recentlyViewedFor(userId, guestToken, excludeProductId));
    }

    private UUID resolveUserId(UserDetails principal) {
        if (principal == null) {
            return null;
        }
        return currentUserQueryService.getByEmail(principal.getUsername()).getId();
    }
}
