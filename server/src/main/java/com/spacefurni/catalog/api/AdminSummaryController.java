package com.spacefurni.catalog.api;

import com.spacefurni.catalog.api.dto.AdminSummaryResponse;
import com.spacefurni.catalog.application.AdminProductService;
import com.spacefurni.checkout.application.AdminOrderQueryService;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.shared.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/summary")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSummaryController {

    private final AdminProductService adminProductService;
    private final InventoryService inventoryService;
    private final AdminOrderQueryService adminOrderQueryService;

    public AdminSummaryController(AdminProductService adminProductService, InventoryService inventoryService,
            AdminOrderQueryService adminOrderQueryService) {
        this.adminProductService = adminProductService;
        this.inventoryService = inventoryService;
        this.adminOrderQueryService = adminOrderQueryService;
    }

    @GetMapping
    public ApiResponse<AdminSummaryResponse> getSummary() {
        long pendingOrdersCount = adminOrderQueryService.countOrdersByStatus().getOrDefault(OrderStatus.PENDING, 0L);
        return ApiResponse.success(new AdminSummaryResponse(adminProductService.countPublishedProducts(),
                adminOrderQueryService.countOrdersPlacedToday(), pendingOrdersCount,
                inventoryService.countLowStockItems()));
    }
}
