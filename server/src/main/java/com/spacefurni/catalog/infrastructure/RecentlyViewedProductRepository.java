package com.spacefurni.catalog.infrastructure;

import com.spacefurni.catalog.domain.RecentlyViewedProduct;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecentlyViewedProductRepository extends JpaRepository<RecentlyViewedProduct, UUID> {

    @Modifying
    @Query(value = "insert into recently_viewed_products (user_id, product_id, viewed_at) "
            + "values (:userId, :productId, clock_timestamp()) "
            + "on conflict (user_id, product_id) do update set viewed_at = clock_timestamp()", nativeQuery = true)
    void upsertViewForUser(@Param("userId") UUID userId, @Param("productId") UUID productId);

    @Modifying
    @Query(value = "insert into recently_viewed_products (guest_token, product_id, viewed_at) "
            + "values (:guestToken, :productId, clock_timestamp()) "
            + "on conflict (guest_token, product_id) do update set viewed_at = clock_timestamp()", nativeQuery = true)
    void upsertViewForGuest(@Param("guestToken") UUID guestToken, @Param("productId") UUID productId);

    @Modifying
    @Query(value = "delete from recently_viewed_products "
            + "where user_id = :userId and id not in "
            + "(select id from recently_viewed_products where user_id = :userId "
            + "order by viewed_at desc limit :retainedCount)", nativeQuery = true)
    void evictBeyondRetainedCountForUser(@Param("userId") UUID userId, @Param("retainedCount") int retainedCount);

    @Modifying
    @Query(value = "delete from recently_viewed_products "
            + "where guest_token = :guestToken and id not in "
            + "(select id from recently_viewed_products where guest_token = :guestToken "
            + "order by viewed_at desc limit :retainedCount)", nativeQuery = true)
    void evictBeyondRetainedCountForGuest(@Param("guestToken") UUID guestToken,
            @Param("retainedCount") int retainedCount);

    List<RecentlyViewedProduct> findByUserIdOrderByViewedAtDesc(UUID userId, Pageable pageable);

    List<RecentlyViewedProduct> findByGuestTokenOrderByViewedAtDesc(UUID guestToken, Pageable pageable);

    List<RecentlyViewedProduct> findByUserIdAndProductIdNotOrderByViewedAtDesc(UUID userId, UUID excludedProductId,
            Pageable pageable);

    List<RecentlyViewedProduct> findByGuestTokenAndProductIdNotOrderByViewedAtDesc(UUID guestToken,
            UUID excludedProductId, Pageable pageable);
}
