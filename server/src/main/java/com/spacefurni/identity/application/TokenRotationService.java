package com.spacefurni.identity.application;

import com.spacefurni.identity.api.dto.AuthenticationResponse;
import com.spacefurni.identity.domain.RefreshToken;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.infrastructure.RefreshTokenRepository;
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
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenRotationService {

    private static final int REFRESH_TOKEN_BYTE_LENGTH = 48;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenRotationService(
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthenticationResponse refresh(String rawRefreshToken) {
        RefreshToken presentedToken = findByRawToken(rawRefreshToken);

        if (presentedToken.isRevoked()) {
            revokeAllActiveTokensForUser(presentedToken.getUser());
            throw new InvalidRefreshTokenException();
        }
        if (presentedToken.isExpired()) {
            throw new InvalidRefreshTokenException();
        }

        presentedToken.revoke();
        User user = presentedToken.getUser();
        String accessToken =
                jwtTokenProvider.generateAccessToken(user.getId().toString(), user.getEmail(), user.getRole());
        String refreshToken = issueRefreshToken(user);

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        findByRawToken(rawRefreshToken).revoke();
    }

    private RefreshToken findByRawToken(String rawRefreshToken) {
        return refreshTokenRepository
                .findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    private void revokeAllActiveTokensForUser(User user) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user);
        activeTokens.forEach(RefreshToken::revoke);
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
