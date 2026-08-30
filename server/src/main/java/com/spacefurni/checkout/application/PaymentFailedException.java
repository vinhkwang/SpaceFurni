package com.spacefurni.checkout.application;

import com.spacefurni.shared.exception.DomainException;
import com.spacefurni.shared.exception.ErrorCode;

public class PaymentFailedException extends DomainException {

    public PaymentFailedException(String failureReason) {
        super(ErrorCode.PAYMENT_FAILED, failureReason);
    }
}
