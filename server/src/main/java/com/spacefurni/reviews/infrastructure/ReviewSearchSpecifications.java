package com.spacefurni.reviews.infrastructure;

import com.spacefurni.reviews.domain.Review;
import com.spacefurni.reviews.domain.ReviewStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ReviewSearchSpecifications {

    private ReviewSearchSpecifications() {
    }

    public static Specification<Review> hasStatus(ReviewStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Review> hasProductId(UUID productId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("productId"), productId);
    }
}
