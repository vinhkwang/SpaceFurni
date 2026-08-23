package com.spacefurni.identity.application;

import com.spacefurni.shared.exception.DomainException;
import com.spacefurni.shared.exception.ErrorCode;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super(ErrorCode.UNAUTHENTICATED, "Invalid email or password");
    }
}
