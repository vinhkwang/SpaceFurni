package com.spacefurni.identity.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RegisterRequestTest {

    private final Validator validator;

    RegisterRequestTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    @Test
    void acceptsAValidRequest() {
        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(new RegisterRequest("jane@example.com", "password123", "Jane Doe"));

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsAMalformedEmail() {
        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(new RegisterRequest("not-an-email", "password123", "Jane Doe"));

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsAPasswordShorterThanEightCharacters() {
        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(new RegisterRequest("jane@example.com", "short", "Jane Doe"));

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsABlankFullName() {
        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(new RegisterRequest("jane@example.com", "password123", " "));

        assertThat(violations).isNotEmpty();
    }
}
