package com.spacefurni.catalog.infrastructure;

import com.spacefurni.catalog.domain.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = { "category", "images", "specifications", "colorSwatches" })
    Optional<Product> findBySlug(String slug);

    @EntityGraph(attributePaths = { "category", "images" })
    List<Product> findAllByIdIn(List<UUID> ids);

    Optional<Product> findTopBySkuStartingWithOrderBySkuDesc(String skuPrefix);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> lockForRatingRecompute(@Param("productId") UUID productId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE products
               SET rating_average = COALESCE(
                       (SELECT ROUND(AVG(rating), 1) FROM reviews
                         WHERE product_id = :productId AND status = 'PUBLISHED'), 0),
                   review_count = (SELECT COUNT(*) FROM reviews
                                    WHERE product_id = :productId AND status = 'PUBLISHED')
             WHERE id = :productId
            """, nativeQuery = true)
    int recomputeRatingAggregate(@Param("productId") UUID productId);

    @Query(value = """
            SELECT other_item.product_id AS productId, COUNT(*) AS occurrences
              FROM order_items viewed_item
              JOIN order_items other_item
                ON other_item.order_id = viewed_item.order_id
               AND other_item.product_id <> viewed_item.product_id
              JOIN products other_product ON other_product.id = other_item.product_id
             WHERE viewed_item.product_id = :productId
               AND other_product.status <> 'ARCHIVED'
             GROUP BY other_item.product_id
             ORDER BY occurrences DESC
             LIMIT :limit
            """, nativeQuery = true)
    List<CoPurchaseRow> findCoPurchasedProducts(@Param("productId") UUID productId, @Param("limit") int limit);

    interface CoPurchaseRow {
        UUID getProductId();

        long getOccurrences();
    }
}
