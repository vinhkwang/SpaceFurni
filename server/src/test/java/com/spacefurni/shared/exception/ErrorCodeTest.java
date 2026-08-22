package com.spacefurni.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorCodeTest {

    @Test
    void containsEverySpecErrorCode() {
        Set<String> specCodes = Set.of(
                "VALIDATION_FAILED",
                "UNAUTHENTICATED",
                "FORBIDDEN",
                "RESOURCE_NOT_FOUND",
                "INSUFFICIENT_STOCK",
                "CONCURRENT_MODIFICATION",
                "DUPLICATE_RESOURCE",
                "BUSINESS_RULE_VIOLATION",
                "PROMOTION_NOT_APPLICABLE",
                "PAYMENT_FAILED",
                "INTERNAL_ERROR");

        Set<String> enumCodes = Arrays.stream(ErrorCode.values()).map(Enum::name).collect(Collectors.toSet());

        assertThat(enumCodes).containsAll(specCodes);
    }

    @Test
    void everyCodeCarriesItsSpecHttpStatus() {
        assertThat(ErrorCode.VALIDATION_FAILED.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorCode.UNAUTHENTICATED.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.FORBIDDEN.httpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.INSUFFICIENT_STOCK.httpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.CONCURRENT_MODIFICATION.httpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.DUPLICATE_RESOURCE.httpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.BUSINESS_RULE_VIOLATION.httpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorCode.PROMOTION_NOT_APPLICABLE.httpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorCode.PAYMENT_FAILED.httpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorCode.INTERNAL_ERROR.httpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
