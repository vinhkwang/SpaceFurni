package com.spacefurni.checkout.domain;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BankTransferPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult execute(Order order) {
        return new PaymentResult(PaymentStatus.PENDING, "TRANSFER-" + UUID.randomUUID(), null);
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.BANK_TRANSFER;
    }
}
