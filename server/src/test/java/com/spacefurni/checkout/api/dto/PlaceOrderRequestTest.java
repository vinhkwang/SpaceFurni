package com.spacefurni.checkout.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.PaymentMethod;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PlaceOrderRequestTest {

    private final Validator validator;

    PlaceOrderRequestTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    private DeliveryDetailsRequest validDeliveryDetails() {
        return new DeliveryDetailsRequest("Nguyen Van A", "0901234567", "1 Le Loi", "District 1", "Ho Chi Minh City",
                null);
    }

    @Test
    void acceptsAValidRequest() {
        Set<ConstraintViolation<PlaceOrderRequest>> violations = validator.validate(
                new PlaceOrderRequest(validDeliveryDetails(), DeliveryWindow.STANDARD, PaymentMethod.CARD));

        assertThat(violations).isEmpty();
    }

    @Test
    void acceptsAnOptionalNote() {
        DeliveryDetailsRequest withNote = new DeliveryDetailsRequest("Nguyen Van A", "0901234567", "1 Le Loi",
                "District 1", "Ho Chi Minh City", "Leave at the front desk");

        Set<ConstraintViolation<PlaceOrderRequest>> violations =
                validator.validate(new PlaceOrderRequest(withNote, DeliveryWindow.NEXT_DAY, PaymentMethod.CARD));

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsAMalformedPhoneNumber() {
        DeliveryDetailsRequest invalidPhone = new DeliveryDetailsRequest("Nguyen Van A", "not-a-phone-number",
                "1 Le Loi", "District 1", "Ho Chi Minh City", null);

        Set<ConstraintViolation<PlaceOrderRequest>> violations = validator.validate(
                new PlaceOrderRequest(invalidPhone, DeliveryWindow.STANDARD, PaymentMethod.CARD));

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsABlankStreet() {
        DeliveryDetailsRequest blankStreet =
                new DeliveryDetailsRequest("Nguyen Van A", "0901234567", " ", "District 1", "Ho Chi Minh City", null);

        Set<ConstraintViolation<PlaceOrderRequest>> violations = validator.validate(
                new PlaceOrderRequest(blankStreet, DeliveryWindow.STANDARD, PaymentMethod.CARD));

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsAMissingDeliveryWindow() {
        Set<ConstraintViolation<PlaceOrderRequest>> violations =
                validator.validate(new PlaceOrderRequest(validDeliveryDetails(), null, PaymentMethod.CARD));

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsAMissingPaymentMethod() {
        Set<ConstraintViolation<PlaceOrderRequest>> violations =
                validator.validate(new PlaceOrderRequest(validDeliveryDetails(), DeliveryWindow.STANDARD, null));

        assertThat(violations).isNotEmpty();
    }
}
