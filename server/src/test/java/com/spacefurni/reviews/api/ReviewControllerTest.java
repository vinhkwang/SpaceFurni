package com.spacefurni.reviews.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.identity.security.SecurityConfiguration;
import com.spacefurni.identity.security.SpaceFurniUserDetailsService;
import com.spacefurni.reviews.api.dto.RatingHistogramResponse;
import com.spacefurni.reviews.api.dto.ReviewResponse;
import com.spacefurni.reviews.api.mapper.ReviewResponseMapper;
import com.spacefurni.reviews.application.ReviewQueryService;
import com.spacefurni.reviews.application.ReviewService;
import com.spacefurni.reviews.domain.InvalidReviewException;
import com.spacefurni.reviews.domain.Review;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class ReviewControllerTest {

    private static final String EMAIL = "user@spacefurni.com";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvcWithSecurity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private ReviewQueryService reviewQueryService;

    @MockitoBean
    private ReviewResponseMapper reviewResponseMapper;

    @MockitoBean
    private CurrentUserQueryService currentUserQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    private UUID stubUser() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(currentUserQueryService.getByEmail(EMAIL)).thenReturn(user);
        return userId;
    }

    private ReviewResponse dummyReviewResponse(UUID productId) {
        return new ReviewResponse(UUID.randomUUID(), productId, UUID.randomUUID(), (short) 4, "Solid build",
                Instant.now());
    }

    @Test
    void submitReviewIsRejectedWithoutAuthentication() throws Exception {
        UUID productId = UUID.randomUUID();
        String body = "{\"orderItemId\": \"" + UUID.randomUUID() + "\", \"rating\": 5}";

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", productId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reviewService);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void submitReviewRejectsAnOutOfRangeRating() throws Exception {
        UUID productId = UUID.randomUUID();
        String body = "{\"orderItemId\": \"" + UUID.randomUUID() + "\", \"rating\": 7}";

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", productId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));

        verifyNoInteractions(reviewService);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void submitReviewDelegatesToReviewServiceAndReturnsTheMappedResponse() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = stubUser();
        UUID orderItemId = UUID.randomUUID();
        String body = "{\"orderItemId\": \"" + orderItemId + "\", \"rating\": 4, \"comment\": \"Solid build\"}";
        Review review = mock(Review.class);
        when(reviewService.submitReview(eq(userId), eq(orderItemId), eq((short) 4), eq("Solid build")))
                .thenReturn(review);
        when(reviewResponseMapper.toResponse(review)).thenReturn(dummyReviewResponse(productId));

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", productId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4))
                .andExpect(jsonPath("$.data.comment").value("Solid build"));

        verify(reviewService).submitReview(eq(userId), eq(orderItemId), eq((short) 4), eq("Solid build"));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void submitReviewPropagatesIneligibilityAsUnprocessableEntity() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = stubUser();
        UUID orderItemId = UUID.randomUUID();
        String body = "{\"orderItemId\": \"" + orderItemId + "\", \"rating\": 4}";
        when(reviewService.submitReview(eq(userId), eq(orderItemId), any(), eq(null)))
                .thenThrow(new InvalidReviewException("Not eligible to review order item " + orderItemId));

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", productId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void listReviewsIsPublicAndReturnsAPagedMappedResult() throws Exception {
        UUID productId = UUID.randomUUID();
        Review review = mock(Review.class);
        when(reviewQueryService.findPublishedReviews(eq(productId), any()))
                .thenReturn(new PageImpl<>(List.of(review), PageRequest.of(0, 10), 1));
        when(reviewResponseMapper.toResponse(review)).thenReturn(dummyReviewResponse(productId));

        mockMvc.perform(get("/api/v1/products/{productId}/reviews", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].rating").value(4))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void ratingHistogramIsPublicAndReturnsAllFiveBuckets() throws Exception {
        UUID productId = UUID.randomUUID();
        Map<Short, Long> histogram = Map.of((short) 5, 2L);
        when(reviewQueryService.findRatingHistogram(productId)).thenReturn(histogram);
        when(reviewResponseMapper.toHistogramResponse(histogram)).thenReturn(
                new RatingHistogramResponse(List.of(new RatingHistogramResponse.StarRatingCount((short) 5, 2L))));

        mockMvc.perform(get("/api/v1/products/{productId}/reviews/histogram", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.counts[0].stars").value(5))
                .andExpect(jsonPath("$.data.counts[0].count").value(2));
    }
}
