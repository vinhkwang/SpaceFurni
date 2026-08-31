package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.shared.api.PageResponse;
import java.util.Map;

public record AdminOrderListResponse(PageResponse<AdminOrderRowResponse> orders,
        Map<OrderStatus, Long> statusCounts) {
}
