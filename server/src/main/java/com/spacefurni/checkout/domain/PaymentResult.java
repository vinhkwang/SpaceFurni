package com.spacefurni.checkout.domain;

public record PaymentResult(PaymentStatus status, String providerReference, String failureReason) {
}
