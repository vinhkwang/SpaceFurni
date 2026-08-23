package com.spacefurni.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.identity.api.dto.AuthenticationResponse;
import com.spacefurni.identity.api.dto.LoginRequest;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
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
class AuthenticationServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private final JwtProperties jwtProperties =
            new JwtProperties("a-secret-that-is-at-least-32-bytes-long", 15, 7, "spacefurni");
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties);

    private AuthenticationService authenticationService() {
        return new AuthenticationService(
                userRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider, jwtProperties);
    }

    @Test
    void logsInWithCorrectCredentialsAndReturnsTokens() {
        userRepository.save(new User(
                "login.success@example.com", passwordEncoder.encode("correct-password"), "Login Success", UserRole.CUSTOMER));

        AuthenticationResponse response =
                authenticationService().login(new LoginRequest("login.success@example.com", "correct-password"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsWrongPasswordWithUnauthenticatedErrorCode() {
        userRepository.save(new User(
                "wrong.password@example.com", passwordEncoder.encode("correct-password"), "Wrong Password", UserRole.CUSTOMER));

        assertThatThrownBy(() ->
                        authenticationService().login(new LoginRequest("wrong.password@example.com", "incorrect")))
                .isInstanceOf(InvalidCredentialsException.class)
                .satisfies(exception -> assertThat(((InvalidCredentialsException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.UNAUTHENTICATED));
    }

    @Test
    void rejectsUnknownEmailWithTheSameMessageAsWrongPassword() {
        userRepository.save(new User(
                "known.user@example.com", passwordEncoder.encode("correct-password"), "Known User", UserRole.CUSTOMER));

        String wrongPasswordMessage = catchMessage(() ->
                authenticationService().login(new LoginRequest("known.user@example.com", "incorrect")));
        String unknownEmailMessage = catchMessage(() ->
                authenticationService().login(new LoginRequest("nobody@example.com", "incorrect")));

        assertThat(unknownEmailMessage).isEqualTo(wrongPasswordMessage);
    }

    private String catchMessage(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected InvalidCredentialsException to be thrown");
        } catch (InvalidCredentialsException exception) {
            return exception.getMessage();
        }
    }
}
