package com.spacefurni.shared.exception;

import jakarta.validation.constraints.NotBlank;

record GlobalExceptionHandlerTestRequest(@NotBlank String name) {
}
