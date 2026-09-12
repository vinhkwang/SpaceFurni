package com.spacefurni.checkout.domain;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EWalletPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult execute(Order order) {
        return new PaymentResult(PaymentStatus.CAPTURED, "EWALLET-" + UUID.randomUUID(), null);
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.E_WALLET;
    }
}
