package com.spacefurni.identity.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.identity.api.dto.AuthenticationResponse;
import com.spacefurni.identity.application.AuthenticationService;
import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.identity.application.RegistrationService;
import com.spacefurni.identity.application.TokenRotationService;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.identity.security.SecurityConfiguration;
import com.spacefurni.identity.security.SpaceFurniUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class AuthenticationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvcWithSecurity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private TokenRotationService tokenRotationService;

    @MockitoBean
    private CurrentUserQueryService currentUserQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    @Test
    void registerDelegatesToRegistrationServiceAndReturnsTokens() throws Exception {
        when(registrationService.register(any())).thenReturn(new AuthenticationResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                { "email": "new@spacefurni.com", "password": "password1", "fullName": "New User" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void loginDelegatesToAuthenticationServiceAndReturnsTokens() throws Exception {
        when(authenticationService.login(any())).thenReturn(new AuthenticationResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                { "email": "user@spacefurni.com", "password": "password1" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void refreshDelegatesToTokenRotationService() throws Exception {
        when(tokenRotationService.refresh(eq("old-refresh-token")))
                .thenReturn(new AuthenticationResponse("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                { "refreshToken": "old-refresh-token" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void logoutIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                { "refreshToken": "some-refresh-token" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@spacefurni.com")
    void logoutDelegatesToTokenRotationServiceWhenAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                { "refreshToken": "some-refresh-token" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(tokenRotationService).logout("some-refresh-token");
    }

    @Test
    void currentUserIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@spacefurni.com")
    void currentUserReturnsAuthenticatedPrincipal() throws Exception {
        User user = new User("user@spacefurni.com", "hash", "Current User", UserRole.CUSTOMER);
        when(currentUserQueryService.getByEmail("user@spacefurni.com")).thenReturn(user);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user@spacefurni.com"))
                .andExpect(jsonPath("$.data.fullName").value("Current User"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }
}
