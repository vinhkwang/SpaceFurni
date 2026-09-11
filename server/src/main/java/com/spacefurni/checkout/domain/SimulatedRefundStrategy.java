package com.spacefurni.checkout.domain;

import com.spacefurni.shared.domain.Money;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SimulatedRefundStrategy implements RefundStrategy {

    @Override
    public void refund(Order order, Money amount) {
    }

    @Override
    public Set<PaymentMethod> supportedMethods() {
        return EnumSet.allOf(PaymentMethod.class);
    }
}
