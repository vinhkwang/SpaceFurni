package com.spacefurni.catalog.infrastructure;

import com.spacefurni.catalog.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = { "category", "images", "specifications", "colorSwatches" })
    Optional<Product> findBySlug(String slug);

    @EntityGraph(attributePaths = { "category", "images" })
    List<Product> findAllByIdIn(List<UUID> ids);

    Optional<Product> findTopBySkuStartingWithOrderBySkuDesc(String skuPrefix);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Product p
               SET p.ratingAverage = :ratingAverage,
                   p.reviewCount = :reviewCount
             WHERE p.id = :productId
            """)
    int updateRatingAggregate(@Param("productId") UUID productId, @Param("ratingAverage") BigDecimal ratingAverage,
            @Param("reviewCount") int reviewCount);

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
