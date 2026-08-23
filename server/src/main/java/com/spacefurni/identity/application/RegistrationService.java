package com.spacefurni.identity.application;

import com.spacefurni.identity.api.dto.AuthenticationResponse;
import com.spacefurni.identity.api.dto.RegisterRequest;
import com.spacefurni.identity.domain.RefreshToken;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private static final int REFRESH_TOKEN_BYTE_LENGTH = 48;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegistrationService(
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
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = userRepository.save(new User(
                request.email(), passwordEncoder.encode(request.password()), request.fullName(), UserRole.CUSTOMER));

        String accessToken =
                jwtTokenProvider.generateAccessToken(user.getId().toString(), user.getEmail(), user.getRole());
        String refreshToken = issueRefreshToken(user);

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
