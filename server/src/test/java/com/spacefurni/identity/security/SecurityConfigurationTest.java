package com.spacefurni.identity.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SecurityConfigurationTestController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    @Test
    void unauthenticatedRequestToProtectedRouteIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/orders/test-protected-probe")).andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedRequestToPublicProductsRouteSucceeds() throws Exception {
        mockMvc.perform(get("/api/v1/products/test-public-probe")).andExpect(status().isOk());
    }
}
