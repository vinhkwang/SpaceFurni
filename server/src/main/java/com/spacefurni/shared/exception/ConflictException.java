package com.spacefurni.shared.exception;

import java.util.Map;

public abstract class ConflictException extends DomainException {

    protected ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected ConflictException(ErrorCode errorCode, String message, Map<String, String> details) {
        super(errorCode, message, details);
    }
}
