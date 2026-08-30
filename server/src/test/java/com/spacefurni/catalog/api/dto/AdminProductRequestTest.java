package com.spacefurni.catalog.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.domain.ProductStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AdminProductRequestTest {

    private final Validator validator;

    AdminProductRequestTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    private AdminProductRequest validRequest() {
        return new AdminProductRequest("Halden Tub Chair", "living-room", "armchairs", 7_200_000L, 12,
                "A comfortable tub chair.", "Long description.", "80x75x70cm", "Oak, linen", "Terracotta",
                "https://example.com/chair.jpg", ProductStatus.PUBLISHED, null);
    }

    @Test
    void acceptsAValidRequest() {
        Set<ConstraintViolation<AdminProductRequest>> violations = validator.validate(validRequest());

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsABlankTitle() {
        AdminProductRequest blankTitle = new AdminProductRequest(" ", "living-room", "armchairs", 7_200_000L, 12,
                null, null, null, null, null, null, ProductStatus.PUBLISHED, null);

        Set<ConstraintViolation<AdminProductRequest>> violations = validator.validate(blankTitle);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsABlankDepartment() {
        AdminProductRequest blankDepartment = new AdminProductRequest("Halden Tub Chair", " ", "armchairs",
                7_200_000L, 12, null, null, null, null, null, null, ProductStatus.PUBLISHED, null);

        Set<ConstraintViolation<AdminProductRequest>> violations = validator.validate(blankDepartment);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsAZeroPrice() {
        AdminProductRequest zeroPrice = new AdminProductRequest("Halden Tub Chair", "living-room", "armchairs", 0L,
                12, null, null, null, null, null, null, ProductStatus.PUBLISHED, null);

        Set<ConstraintViolation<AdminProductRequest>> violations = validator.validate(zeroPrice);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsANegativePrice() {
        AdminProductRequest negativePrice = new AdminProductRequest("Halden Tub Chair", "living-room", "armchairs",
                -1L, 12, null, null, null, null, null, null, ProductStatus.PUBLISHED, null);

        Set<ConstraintViolation<AdminProductRequest>> violations = validator.validate(negativePrice);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void rejectsANegativeStock() {
        AdminProductRequest negativeStock = new AdminProductRequest("Halden Tub Chair", "living-room", "armchairs",
                7_200_000L, -1, null, null, null, null, null, null, ProductStatus.PUBLISHED, null);

        Set<ConstraintViolation<AdminProductRequest>> violations = validator.validate(negativeStock);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void acceptsZeroStock() {
        AdminProductRequest zeroStock = new AdminProductRequest("Halden Tub Chair", "living-room", "armchairs",
                7_200_000L, 0, null, null, null, null, null, null, ProductStatus.PUBLISHED, null);

        Set<ConstraintViolation<AdminProductRequest>> violations = validator.validate(zeroStock);

        assertThat(violations).isEmpty();
    }

    @Test
    void acceptsAMissingSubCategory() {
        AdminProductRequest noSubCategory = new AdminProductRequest("Halden Tub Chair", "living-room", null,
                7_200_000L, 12, null, null, null, null, null, null, ProductStatus.PUBLISHED, null);

        Set<ConstraintViolation<AdminProductRequest>> violations = validator.validate(noSubCategory);

        assertThat(violations).isEmpty();
    }
}
