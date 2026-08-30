package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotNull @Valid DeliveryDetailsRequest deliveryDetails,

        @NotNull DeliveryWindow deliveryWindow,

        @NotNull PaymentMethod paymentMethod) {
}
