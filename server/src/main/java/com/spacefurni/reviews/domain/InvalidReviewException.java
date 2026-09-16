package com.spacefurni.reviews.domain;

import com.spacefurni.shared.exception.DomainException;
import com.spacefurni.shared.exception.ErrorCode;

public class InvalidReviewException extends DomainException {

    public InvalidReviewException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
