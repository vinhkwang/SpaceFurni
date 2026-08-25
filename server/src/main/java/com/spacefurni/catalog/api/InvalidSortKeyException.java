package com.spacefurni.catalog.api;

import com.spacefurni.shared.exception.DomainException;
import com.spacefurni.shared.exception.ErrorCode;

public class InvalidSortKeyException extends DomainException {

    public InvalidSortKeyException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message);
    }
}
