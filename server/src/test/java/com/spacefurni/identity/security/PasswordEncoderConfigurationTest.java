package com.spacefurni.identity.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderConfigurationTest {

    private final PasswordEncoder passwordEncoder = new PasswordEncoderConfiguration().passwordEncoder();

    @Test
    void encodesWithBcryptStrengthTwelve() {
        String encoded = passwordEncoder.encode("correct-horse-battery-staple");

        assertThat(encoded).startsWith("$2a$12$");
    }

    @Test
    void matchesRawPasswordAgainstItsOwnEncodedHash() {
        String encoded = passwordEncoder.encode("correct-horse-battery-staple");

        assertThat(passwordEncoder.matches("correct-horse-battery-staple", encoded)).isTrue();
        assertThat(passwordEncoder.matches("wrong-password", encoded)).isFalse();
    }
}
