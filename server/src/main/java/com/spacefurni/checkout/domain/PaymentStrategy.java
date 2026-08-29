package com.spacefurni.checkout.domain;

public interface PaymentStrategy {

    PaymentResult execute(Order order);

    PaymentMethod supportedMethod();
}
