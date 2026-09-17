package com.spacefurni.reviews.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.identity.security.SecurityConfiguration;
import com.spacefurni.identity.security.SpaceFurniUserDetailsService;
import com.spacefurni.reviews.api.dto.AdminReviewRowResponse;
import com.spacefurni.reviews.application.AdminReviewService;
import com.spacefurni.reviews.domain.ReviewStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = AdminReviewController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class AdminReviewControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvcWithSecurity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @MockitoBean
    private AdminReviewService adminReviewService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    @Test
    void listReviewsIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reviews")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listReviewsIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reviews")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listReviewsFiltersByStatusAndReturnsPagedRows() throws Exception {
        UUID productId = UUID.randomUUID();
        AdminReviewRowResponse row = new AdminReviewRowResponse(UUID.randomUUID(), productId, UUID.randomUUID(),
                (short) 4, "Solid build", ReviewStatus.PUBLISHED, Instant.parse("2026-08-12T02:24:00Z"));
        when(adminReviewService.listReviews(eq(ReviewStatus.PUBLISHED), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(row)));

        mockMvc.perform(get("/api/v1/admin/reviews").param("status", "PUBLISHED")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].rating").value(4));

        verify(adminReviewService).listReviews(eq(ReviewStatus.PUBLISHED), isNull(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatusDelegatesToTheServiceWithTheTargetStatus() throws Exception {
        UUID reviewId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/reviews/{id}/status", reviewId).with(csrf()).param("status", "HIDDEN"))
                .andExpect(status().isOk());

        verify(adminReviewService).updateStatus(reviewId, ReviewStatus.HIDDEN);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateStatusIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/admin/reviews/{id}/status", UUID.randomUUID()).with(csrf()).param("status", "HIDDEN"))
                .andExpect(status().isForbidden());
    }
}
