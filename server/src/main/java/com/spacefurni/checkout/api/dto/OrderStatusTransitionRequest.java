package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusTransitionRequest(@NotNull OrderStatus status, @NotNull Long version) {
}
