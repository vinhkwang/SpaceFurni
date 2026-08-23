package com.spacefurni.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registerLoginAccessProtectedRouteRefreshRotatesAndRejectsReplayThenLogout() throws Exception {
        String email = "flow-user@spacefurni.com";

        JsonNode registerData = performAndReadData(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        { "email": "%s", "password": "password1", "fullName": "Flow User" }
                        """
                                .formatted(email)));
        String firstAccessToken = registerData.get("accessToken").asString();
        String firstRefreshToken = registerData.get("refreshToken").asString();

        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + firstAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        JsonNode refreshData = performAndReadData(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        { "refreshToken": "%s" }
                        """
                                .formatted(firstRefreshToken)));
        String rotatedAccessToken = refreshData.get("accessToken").asString();
        String rotatedRefreshToken = refreshData.get("refreshToken").asString();
        assertThat(rotatedRefreshToken).isNotEqualTo(firstRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                { "refreshToken": "%s" }
                                """
                                        .formatted(firstRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + rotatedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                { "refreshToken": "%s" }
                                """
                                        .formatted(rotatedRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void registeringWithAnAlreadyRegisteredEmailIsRejected() throws Exception {
        String email = "duplicate-user@spacefurni.com";
        String requestBody =
                """
                { "email": "%s", "password": "password1", "fullName": "Duplicate User" }
                """
                        .formatted(email);

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void loggingInWithTheWrongPasswordIsRejected() throws Exception {
        String email = "wrong-password-user@spacefurni.com";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                { "email": "%s", "password": "correct-password", "fullName": "Wrong Password User" }
                                """
                                        .formatted(email)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                { "email": "%s", "password": "not-the-right-password" }
                                """
                                        .formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    private JsonNode performAndReadData(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        MvcResult result =
                mockMvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }
}
