package com.spacefurni.cart.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddCartLineRequest(@NotNull UUID productId, @NotNull @Min(1) Integer quantity, String colorHexCode) {
}
