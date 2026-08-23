package com.spacefurni.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.identity.api.dto.AuthenticationResponse;
import com.spacefurni.identity.domain.RefreshToken;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.RefreshTokenRepository;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.identity.security.JwtProperties;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class TokenRotationServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final JwtProperties jwtProperties =
            new JwtProperties("a-secret-that-is-at-least-32-bytes-long", 15, 7, "spacefurni");
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties);

    private TokenRotationService tokenRotationService() {
        return new TokenRotationService(refreshTokenRepository, jwtTokenProvider, jwtProperties);
    }

    private User saveUser(String email) {
        return userRepository.save(new User(email, "hash", "Test User", UserRole.CUSTOMER));
    }

    private String issueActiveRawToken(User user) {
        String rawToken = "raw-" + user.getId() + "-" + refreshTokenRepository.count();
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        refreshTokenRepository.save(new RefreshToken(user, hashToken(rawToken), expiresAt));
        return rawToken;
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

    @Test
    void refreshRotatesTokenAndRevokesThePresentedOne() {
        User user = saveUser("rotate.success@example.com");
        String originalRawToken = issueActiveRawToken(user);

        AuthenticationResponse response = tokenRotationService().refresh(originalRawToken);

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotEqualTo(originalRawToken);
        assertThat(refreshTokenRepository.findByTokenHash(hashToken(originalRawToken)))
                .get()
                .satisfies(token -> assertThat(token.isRevoked()).isTrue());
        assertThat(refreshTokenRepository.count()).isEqualTo(2);
    }

    @Test
    void refreshRejectsAnUnknownToken() {
        assertThatThrownBy(() -> tokenRotationService().refresh("no-such-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .satisfies(exception -> assertThat(((InvalidRefreshTokenException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.UNAUTHENTICATED));
    }

    @Test
    void refreshRejectsAnExpiredToken() {
        User user = saveUser("expired.token@example.com");
        String rawToken = "expired-raw-token";
        refreshTokenRepository.save(new RefreshToken(user, hashToken(rawToken), Instant.now().minus(1, ChronoUnit.DAYS)));

        assertThatThrownBy(() -> tokenRotationService().refresh(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void replayingARevokedTokenRevokesEveryActiveTokenForThatUser() {
        User user = saveUser("replay.victim@example.com");
        String firstRawToken = issueActiveRawToken(user);
        String secondRawToken = issueActiveRawToken(user);

        tokenRotationService().refresh(firstRawToken);

        assertThatThrownBy(() -> tokenRotationService().refresh(firstRawToken))
                .isInstanceOf(InvalidRefreshTokenException.class);

        assertThat(refreshTokenRepository.findByTokenHash(hashToken(secondRawToken)))
                .get()
                .satisfies(token -> assertThat(token.isRevoked()).isTrue());
    }

    @Test
    void logoutRevokesThePresentedToken() {
        User user = saveUser("logout.success@example.com");
        String rawToken = issueActiveRawToken(user);

        tokenRotationService().logout(rawToken);

        assertThat(refreshTokenRepository.findByTokenHash(hashToken(rawToken)))
                .get()
                .satisfies(token -> assertThat(token.isRevoked()).isTrue());
    }

    @Test
    void logoutRejectsAnUnknownToken() {
        assertThatThrownBy(() -> tokenRotationService().logout("no-such-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
