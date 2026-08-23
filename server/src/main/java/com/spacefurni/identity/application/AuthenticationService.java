package com.spacefurni.identity.application;

import com.spacefurni.identity.api.dto.AuthenticationResponse;
import com.spacefurni.identity.api.dto.LoginRequest;
import com.spacefurni.identity.domain.RefreshToken;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.infrastructure.RefreshTokenRepository;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.identity.security.JwtProperties;
import com.spacefurni.identity.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final int REFRESH_TOKEN_BYTE_LENGTH = 48;
    private static final String TIMING_SAFETY_DUMMY_PASSWORD = "timing-safety-dummy-password";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String dummyPasswordHash;

    public AuthenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.dummyPasswordHash = passwordEncoder.encode(TIMING_SAFETY_DUMMY_PASSWORD);
    }

    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByEmailIgnoreCase(request.email());
        String hashToVerify = user.map(User::getPasswordHash).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToVerify);

        if (user.isEmpty() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        User authenticatedUser = user.get();
        String accessToken = jwtTokenProvider.generateAccessToken(
                authenticatedUser.getId().toString(), authenticatedUser.getEmail(), authenticatedUser.getRole());
        String refreshToken = issueRefreshToken(authenticatedUser);

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    private String issueRefreshToken(User user) {
        String rawToken = generateRawToken();
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenTtlDays(), ChronoUnit.DAYS);
        refreshTokenRepository.save(new RefreshToken(user, hashToken(rawToken), expiresAt));
        return rawToken;
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[REFRESH_TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }
}
