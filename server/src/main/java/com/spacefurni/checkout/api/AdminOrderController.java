package com.spacefurni.checkout.api;

import com.spacefurni.checkout.api.dto.AdminOrderDetailResponse;
import com.spacefurni.checkout.api.dto.AdminOrderListResponse;
import com.spacefurni.checkout.api.dto.AdminOrderRowResponse;
import com.spacefurni.checkout.api.dto.OrderStatusTransitionRequest;
import com.spacefurni.checkout.application.AdminOrderQueryService;
import com.spacefurni.checkout.application.AdminOrderService;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.shared.api.ApiResponse;
import com.spacefurni.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminOrderQueryService adminOrderQueryService;
    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderQueryService adminOrderQueryService, AdminOrderService adminOrderService) {
        this.adminOrderQueryService = adminOrderQueryService;
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public ApiResponse<AdminOrderListResponse> listOrders(@RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String q, @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Page<AdminOrderRowResponse> result = adminOrderQueryService.listOrders(status, q,
                PageRequest.of(resolvePage(page), resolvePageSize(size)));
        PageResponse<AdminOrderRowResponse> pageResponse = new PageResponse<>(result.getContent(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
        return ApiResponse
                .success(new AdminOrderListResponse(pageResponse, adminOrderQueryService.countOrdersByStatus()));
    }

    @GetMapping("/{orderNumber}")
    public ApiResponse<AdminOrderDetailResponse> getOrderDetail(@PathVariable String orderNumber) {
        return ApiResponse.success(adminOrderQueryService.findOrderDetail(orderNumber));
    }

    @PatchMapping("/{orderNumber}/status")
    public ApiResponse<Void> transitionStatus(@PathVariable String orderNumber,
            @Valid @RequestBody OrderStatusTransitionRequest request) {
        adminOrderService.transitionOrderStatus(orderNumber, request.status(), request.version());
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
