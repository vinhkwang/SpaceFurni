package com.spacefurni.reviews.infrastructure;

import com.spacefurni.reviews.domain.Review;
import com.spacefurni.reviews.domain.ReviewStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID>, JpaSpecificationExecutor<Review> {

    boolean existsByOrderItemId(UUID orderItemId);

    Page<Review> findByProductIdAndStatusOrderByCreatedAtDesc(UUID productId, ReviewStatus status, Pageable pageable);

    @Query(value = """
            SELECT rating, COUNT(*) AS count
              FROM reviews
             WHERE product_id = :productId
               AND status = 'PUBLISHED'
             GROUP BY rating
            """, nativeQuery = true)
    List<RatingHistogramRow> findRatingHistogramByProductId(@Param("productId") UUID productId);

    interface RatingHistogramRow {
        Short getRating();

        long getCount();
    }
}
