package com.spacefurni.checkout.domain;

import com.spacefurni.shared.domain.Money;
import java.util.Set;

public interface RefundStrategy {

    void refund(Order order, Money amount);

    Set<PaymentMethod> supportedMethods();
}
