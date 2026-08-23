package com.spacefurni.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.identity.api.dto.AuthenticationResponse;
import com.spacefurni.identity.api.dto.RegisterRequest;
import com.spacefurni.identity.infrastructure.RefreshTokenRepository;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.identity.security.JwtProperties;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class RegistrationServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private final JwtProperties jwtProperties =
            new JwtProperties("a-secret-that-is-at-least-32-bytes-long", 15, 7, "spacefurni");
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties);

    private RegistrationService registrationService() {
        return new RegistrationService(
                userRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider, jwtProperties);
    }

    @Test
    void registersNewUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("new.user@example.com", "password123", "New User");

        AuthenticationResponse response = registrationService().register(request);

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(userRepository.findByEmailIgnoreCase("new.user@example.com")).isPresent();
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateEmailWithDuplicateResourceErrorCode() {
        RegisterRequest request = new RegisterRequest("duplicate@example.com", "password123", "Existing User");
        registrationService().register(request);

        assertThatThrownBy(() -> registrationService().register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .satisfies(exception ->
                        assertThat(((DuplicateEmailException) exception).getErrorCode())
                                .isEqualTo(ErrorCode.DUPLICATE_RESOURCE));
    }
}
