package com.spacefurni.checkout.api.dto;

import jakarta.validation.constraints.Positive;

public record RefundOrderRequest(@Positive long amountVnd) {
}
