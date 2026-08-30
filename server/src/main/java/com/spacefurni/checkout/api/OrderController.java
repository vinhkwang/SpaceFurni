package com.spacefurni.checkout.api;

import com.spacefurni.checkout.api.dto.OrderResponse;
import com.spacefurni.checkout.api.dto.OrderSummaryResponse;
import com.spacefurni.checkout.api.dto.PlaceOrderRequest;
import com.spacefurni.checkout.api.mapper.OrderResponseMapper;
import com.spacefurni.checkout.application.CheckoutService;
import com.spacefurni.checkout.application.OrderQueryService;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.identity.application.CurrentUserQueryService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 48;

    private final CheckoutService checkoutService;
    private final OrderQueryService orderQueryService;
    private final OrderResponseMapper orderResponseMapper;
    private final CurrentUserQueryService currentUserQueryService;

    public OrderController(CheckoutService checkoutService, OrderQueryService orderQueryService,
            OrderResponseMapper orderResponseMapper, CurrentUserQueryService currentUserQueryService) {
        this.checkoutService = checkoutService;
        this.orderQueryService = orderQueryService;
        this.orderResponseMapper = orderResponseMapper;
        this.currentUserQueryService = currentUserQueryService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> placeOrder(@AuthenticationPrincipal UserDetails principal,
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody PlaceOrderRequest request) {
        Order order = checkoutService.placeOrder(resolveUserId(principal), idempotencyKey, request);
        return ApiResponse.success(orderResponseMapper.toResponse(order));
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderSummaryResponse>> orderHistory(
            @AuthenticationPrincipal UserDetails principal, @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Page<OrderSummaryResponse> result = orderQueryService.findOrderHistory(resolveUserId(principal),
                PageRequest.of(resolvePage(page), resolvePageSize(size)));
        return ApiResponse.success(new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages()));
    }

    @GetMapping("/{orderNumber}")
    public ApiResponse<OrderResponse> orderDetail(@AuthenticationPrincipal UserDetails principal,
            @PathVariable String orderNumber) {
        return ApiResponse.success(orderQueryService.findOrderDetail(resolveUserId(principal), orderNumber));
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
