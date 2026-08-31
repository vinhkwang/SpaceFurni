package com.spacefurni.catalog.api.dto;

public record AdminSummaryResponse(long publishedProductCount, long ordersTodayCount, long pendingOrdersCount,
        long lowStockProductCount) {
}
