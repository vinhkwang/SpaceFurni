package com.spacefurni.checkout.domain;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentStrategy implements PaymentStrategy {

    public static final String DECLINED_TEST_PHONE_NUMBER = "0000000000";

    @Override
    public PaymentResult execute(Order order) {
        if (DECLINED_TEST_PHONE_NUMBER.equals(order.getDeliveryDetails().getPhone())) {
            return new PaymentResult(PaymentStatus.FAILED, null, "Card declined by test provider");
        }
        return new PaymentResult(PaymentStatus.CAPTURED, "CARD-" + UUID.randomUUID(), null);
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.CARD;
    }
}
