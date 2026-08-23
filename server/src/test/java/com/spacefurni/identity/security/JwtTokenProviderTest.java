package com.spacefurni.identity.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.identity.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private final JwtProperties jwtProperties =
            new JwtProperties("a-secret-that-is-at-least-32-bytes-long", 15, 7, "spacefurni");
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties);

    @Test
    void roundTripsTokenClaims() {
        String token = jwtTokenProvider.generateAccessToken("user-123", "jane@example.com", UserRole.CUSTOMER);

        Claims claims = jwtTokenProvider.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("user-123");
        assertThat(claims.get("email", String.class)).isEqualTo("jane@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
        assertThat(claims.getIssuer()).isEqualTo("spacefurni");
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtTokenProvider.generateAccessToken("user-123", "jane@example.com", UserRole.CUSTOMER);
        int tamperIndex = token.length() / 2;
        char replacementChar = token.charAt(tamperIndex) == 'a' ? 'b' : 'a';
        String tamperedToken = token.substring(0, tamperIndex) + replacementChar + token.substring(tamperIndex + 1);

        assertThatThrownBy(() -> jwtTokenProvider.parseClaims(tamperedToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void failsFastWhenSecretIsShorterThanThirtyTwoBytes() {
        JwtProperties weakProperties = new JwtProperties("too-short", 15, 7, "spacefurni");

        assertThatThrownBy(() -> new JwtTokenProvider(weakProperties)).isInstanceOf(IllegalStateException.class);
    }
}
