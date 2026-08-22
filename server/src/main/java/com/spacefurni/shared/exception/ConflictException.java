package com.spacefurni.shared.exception;

public abstract class ConflictException extends DomainException {

    protected ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
