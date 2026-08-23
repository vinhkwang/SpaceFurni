package com.spacefurni.identity.api.dto;

public record AuthenticationResponse(String accessToken, String refreshToken) {
}
