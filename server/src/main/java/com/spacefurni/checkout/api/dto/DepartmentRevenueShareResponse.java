package com.spacefurni.checkout.api.dto;

import java.math.BigDecimal;

public record DepartmentRevenueShareResponse(String departmentName, long revenueAmount,
        BigDecimal percentageShare) {
}
