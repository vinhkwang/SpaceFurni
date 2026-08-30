package com.spacefurni.checkout.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DeliveryDetailsRequest(
        @NotBlank String fullName,

        @NotBlank
        @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$",
                message = "must be a valid Vietnamese phone number") String phone,

        @NotBlank String street,

        @NotBlank String district,

        @NotBlank String city,

        String note) {
}
