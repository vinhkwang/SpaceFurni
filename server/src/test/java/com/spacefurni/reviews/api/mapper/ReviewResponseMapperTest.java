package com.spacefurni.reviews.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.reviews.api.dto.RatingHistogramResponse;
import com.spacefurni.reviews.api.dto.ReviewResponse;
import com.spacefurni.reviews.domain.Review;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReviewResponseMapperTest {

    private final ReviewResponseMapper mapper = new ReviewResponseMapper();

    @Test
    void toResponseMapsEveryFieldFromTheEntity() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        Review review = new Review(productId, userId, orderItemId, (short) 4, "Solid build");

        ReviewResponse response = mapper.toResponse(review);

        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.rating()).isEqualTo((short) 4);
        assertThat(response.comment()).isEqualTo("Solid build");
        assertThat(response.createdAt()).isEqualTo(review.getCreatedAt());
    }

    @Test
    void toHistogramResponsePreservesInsertionOrder() {
        Map<Short, Long> histogram = new LinkedHashMap<>();
        histogram.put((short) 5, 2L);
        histogram.put((short) 4, 0L);
        histogram.put((short) 3, 1L);

        RatingHistogramResponse response = mapper.toHistogramResponse(histogram);

        assertThat(response.counts()).extracting(RatingHistogramResponse.StarRatingCount::stars)
                .containsExactly((short) 5, (short) 4, (short) 3);
        assertThat(response.counts()).extracting(RatingHistogramResponse.StarRatingCount::count)
                .containsExactly(2L, 0L, 1L);
    }
}
