package com.spacefurni.shared.exception;

import com.spacefurni.shared.api.ApiError;
import com.spacefurni.shared.api.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException exception) {
        log.warn("Domain exception: {}", exception.getMessage());
        ErrorCode errorCode = exception.getErrorCode();
        ApiError error = new ApiError(errorCode.name(), exception.getMessage(), null);
        return ResponseEntity.status(errorCode.httpStatus()).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationFailure(MethodArgumentNotValidException exception) {
        Map<String, String> details = fieldErrorsToDetails(exception);
        ApiError error = new ApiError(ErrorCode.VALIDATION_FAILED.name(), "Validation failed", details);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.httpStatus()).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(
            OptimisticLockingFailureException exception) {
        log.warn("Optimistic locking failure", exception);
        ApiError error = new ApiError(
                ErrorCode.CONCURRENT_MODIFICATION.name(), "The resource was modified concurrently", null);
        return ResponseEntity.status(ErrorCode.CONCURRENT_MODIFICATION.httpStatus())
                .body(ApiResponse.failure(error));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.warn("Data integrity violation", exception);
        ApiError error = new ApiError(ErrorCode.DUPLICATE_RESOURCE.name(), "The resource already exists", null);
        return ResponseEntity.status(ErrorCode.DUPLICATE_RESOURCE.httpStatus()).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected error", exception);
        ApiError error = new ApiError(ErrorCode.INTERNAL_ERROR.name(), "An unexpected error occurred", null);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.httpStatus()).body(ApiResponse.failure(error));
    }

    private Map<String, String> fieldErrorsToDetails(MethodArgumentNotValidException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return details;
    }
}
