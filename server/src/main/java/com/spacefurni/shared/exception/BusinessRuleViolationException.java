package com.spacefurni.shared.exception;

public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
