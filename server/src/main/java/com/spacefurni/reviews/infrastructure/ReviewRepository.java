package com.spacefurni.reviews.infrastructure;

import com.spacefurni.reviews.domain.Review;
import com.spacefurni.reviews.domain.ReviewStatus;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrderItemId(UUID orderItemId);

    Page<Review> findByProductIdAndStatusOrderByCreatedAtDesc(UUID productId, ReviewStatus status, Pageable pageable);

    @Query(value = """
            SELECT COALESCE(ROUND(AVG(rating), 1), 0) AS average, COUNT(*) AS count
              FROM reviews
             WHERE product_id = :productId
               AND status = 'PUBLISHED'
            """, nativeQuery = true)
    RatingAggregateRow findRatingAggregateByProductId(@Param("productId") UUID productId);

    interface RatingAggregateRow {
        BigDecimal getAverage();

        long getCount();
    }
}
