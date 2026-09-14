package com.spacefurni.checkout.api.dto;

import java.time.LocalDate;

public record MonthlyRevenuePointResponse(LocalDate month, long revenueAmount) {
}
