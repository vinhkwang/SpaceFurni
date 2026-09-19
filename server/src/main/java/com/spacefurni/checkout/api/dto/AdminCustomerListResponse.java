package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.CustomerTier;
import com.spacefurni.shared.api.PageResponse;
import java.util.Map;

public record AdminCustomerListResponse(PageResponse<AdminCustomerRowResponse> customers,
        Map<CustomerTier, Long> tierCounts) {
}
