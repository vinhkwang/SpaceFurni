package com.spacefurni.checkout.application;

import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.shared.exception.DomainException;
import com.spacefurni.shared.exception.ErrorCode;

public class OrderNotCancellableException extends DomainException {

    public OrderNotCancellableException(OrderStatus status) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, "Order cannot be cancelled while in status " + status);
    }
}
