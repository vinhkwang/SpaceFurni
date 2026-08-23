package com.spacefurni.identity.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.identity.domain.RefreshToken;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class RefreshTokenRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void findsRefreshTokenByTokenHash() {
        User user = userRepository.save(new User("token.owner@example.com", "hash", "Token Owner", UserRole.CUSTOMER));
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        refreshTokenRepository.save(new RefreshToken(user, "a".repeat(64), expiresAt));

        assertThat(refreshTokenRepository.findByTokenHash("a".repeat(64))).isPresent();
        assertThat(refreshTokenRepository.findByTokenHash("a".repeat(64)).get().getUser().getEmail())
                .isEqualTo("token.owner@example.com");
    }
}
