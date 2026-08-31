package com.spacefurni.checkout.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.checkout.domain.OrderStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OrderStatusTransitionRequestTest {

    private final Validator validator;

    OrderStatusTransitionRequestTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    @Test
    void acceptsAValidRequest() {
        Set<ConstraintViolation<OrderStatusTransitionRequest>> violations =
                validator.validate(new OrderStatusTransitionRequest(OrderStatus.PACKING, 3L));

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsAMissingStatus() {
        Set<ConstraintViolation<OrderStatusTransitionRequest>> violations =
                validator.validate(new OrderStatusTransitionRequest(null, 3L));

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsAMissingVersion() {
        Set<ConstraintViolation<OrderStatusTransitionRequest>> violations =
                validator.validate(new OrderStatusTransitionRequest(OrderStatus.PACKING, null));

        assertThat(violations).isNotEmpty();
    }
}
