package com.spacefurni.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DomainExceptionHierarchyTest {

    @Test
    void resourceNotFoundExceptionFixesItsCode() {
        ResourceNotFoundException exception = new ResourceNotFoundException("product not found");

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("product not found");
    }

    @Test
    void businessRuleViolationExceptionFixesItsCode() {
        BusinessRuleViolationException exception = new BusinessRuleViolationException("illegal transition");

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BUSINESS_RULE_VIOLATION);
        assertThat(exception.getMessage()).isEqualTo("illegal transition");
    }

    @Test
    void conflictExceptionSubclassesFixTheirOwnCode() {
        ConflictException exception = new StockConflictExceptionFixture("out of stock");

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
        assertThat(exception.getMessage()).isEqualTo("out of stock");
    }

    private static final class StockConflictExceptionFixture extends ConflictException {

        private StockConflictExceptionFixture(String message) {
            super(ErrorCode.INSUFFICIENT_STOCK, message);
        }
    }
}
