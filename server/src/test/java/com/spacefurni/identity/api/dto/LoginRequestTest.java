package com.spacefurni.identity.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

    private final Validator validator;

    LoginRequestTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    @Test
    void acceptsAValidRequest() {
        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(new LoginRequest("jane@example.com", "password123"));

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsABlankEmail() {
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(new LoginRequest(" ", "password123"));

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsABlankPassword() {
        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(new LoginRequest("jane@example.com", " "));

        assertThat(violations).isNotEmpty();
    }
}
