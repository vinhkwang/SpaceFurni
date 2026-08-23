package com.spacefurni.identity.api;

import com.spacefurni.identity.api.dto.AuthenticationResponse;
import com.spacefurni.identity.api.dto.CurrentUserResponse;
import com.spacefurni.identity.api.dto.LoginRequest;
import com.spacefurni.identity.api.dto.RefreshTokenRequest;
import com.spacefurni.identity.api.dto.RegisterRequest;
import com.spacefurni.identity.application.AuthenticationService;
import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.identity.application.RegistrationService;
import com.spacefurni.identity.application.TokenRotationService;
import com.spacefurni.identity.domain.User;
import com.spacefurni.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final TokenRotationService tokenRotationService;
    private final CurrentUserQueryService currentUserQueryService;

    public AuthenticationController(
            RegistrationService registrationService,
            AuthenticationService authenticationService,
            TokenRotationService tokenRotationService,
            CurrentUserQueryService currentUserQueryService) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.tokenRotationService = tokenRotationService;
        this.currentUserQueryService = currentUserQueryService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(registrationService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authenticationService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(tokenRotationService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        tokenRotationService.logout(request.refreshToken());
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> currentUser(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUserQueryService.getByEmail(principal.getUsername());
        return ApiResponse.success(new CurrentUserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole()));
    }
}
