package com.spacefurni.catalog.application;

import com.spacefurni.shared.exception.DomainException;
import com.spacefurni.shared.exception.ErrorCode;

public class InvalidProductImageException extends DomainException {

    public InvalidProductImageException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message);
    }
}
