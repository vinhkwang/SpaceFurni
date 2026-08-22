package com.spacefurni.shared.exception;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class GlobalExceptionHandlerTestController {

    @PostMapping("/test/validate")
    String validate(@Valid @RequestBody GlobalExceptionHandlerTestRequest request) {
        return "ok";
    }
}
