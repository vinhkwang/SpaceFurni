package com.spacefurni.identity.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spacefurni.jwt")
public record JwtProperties(String secret, long accessTokenTtlMinutes, long refreshTokenTtlDays, String issuer) {
}
