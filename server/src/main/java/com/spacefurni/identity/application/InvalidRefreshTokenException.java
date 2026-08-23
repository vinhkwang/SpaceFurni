package com.spacefurni.identity.application;

import com.spacefurni.shared.exception.DomainException;
import com.spacefurni.shared.exception.ErrorCode;

public class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException() {
        super(ErrorCode.UNAUTHENTICATED, "Refresh token is invalid, expired, or has been revoked");
    }
}
