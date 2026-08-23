package com.spacefurni.identity.application;

import com.spacefurni.shared.exception.ConflictException;
import com.spacefurni.shared.exception.ErrorCode;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super(ErrorCode.DUPLICATE_RESOURCE, "A user with email %s already exists".formatted(email));
    }
}
