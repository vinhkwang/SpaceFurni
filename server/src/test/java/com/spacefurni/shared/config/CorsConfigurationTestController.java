package com.spacefurni.shared.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CorsConfigurationTestController {

    @GetMapping("/test/cors")
    String ping() {
        return "ok";
    }
}
