package com.spacefurni.identity.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SecurityConfigurationTestController {

    @GetMapping("/api/v1/products/test-public-probe")
    String products() {
        return "ok";
    }

    @GetMapping("/api/v1/orders/test-protected-probe")
    String orders() {
        return "ok";
    }
}
