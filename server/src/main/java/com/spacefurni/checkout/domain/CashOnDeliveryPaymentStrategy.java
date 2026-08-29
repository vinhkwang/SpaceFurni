package com.spacefurni.checkout.domain;

import org.springframework.stereotype.Component;

@Component
public class CashOnDeliveryPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult execute(Order order) {
        return new PaymentResult(PaymentStatus.PENDING, null, null);
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.CASH_ON_DELIVERY;
    }
}
