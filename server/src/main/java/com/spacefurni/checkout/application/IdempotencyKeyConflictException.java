package com.spacefurni.checkout.application;

import com.spacefurni.shared.exception.ConflictException;
import com.spacefurni.shared.exception.ErrorCode;

public class IdempotencyKeyConflictException extends ConflictException {

    public IdempotencyKeyConflictException(String key) {
        super(ErrorCode.DUPLICATE_RESOURCE, "Idempotency key " + key + " was reused with a different request");
    }
}
