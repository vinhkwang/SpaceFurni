package com.spacefurni.cart.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartLineRequest(@NotNull @Min(0) Integer quantity) {
}
