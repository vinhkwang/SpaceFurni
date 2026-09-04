package com.spacefurni.shared.exception;

import java.util.Map;

public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, String> details;

    protected DomainException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    protected DomainException(ErrorCode errorCode, String message, Map<String, String> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
