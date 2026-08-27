package com.spacefurni.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.exception.ErrorCode;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class InsufficientStockExceptionTest {

    @Test
    void carriesProductIdAndQuantitiesAndFixesInsufficientStockCode() {
        UUID productId = UUID.randomUUID();

        InsufficientStockException exception = new InsufficientStockException(productId, 5, 2);

        assertThat(exception.getProductId()).isEqualTo(productId);
        assertThat(exception.getRequestedQuantity()).isEqualTo(5);
        assertThat(exception.getAvailableQuantity()).isEqualTo(2);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
        assertThat(exception.getErrorCode().httpStatus()).isEqualTo(HttpStatus.CONFLICT);
    }
}
