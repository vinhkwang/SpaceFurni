package com.spacefurni.cart.infrastructure;

import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByUserIdAndStatus(UUID userId, CartStatus status);

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByGuestTokenAndStatus(UUID guestToken, CartStatus status);

    @Modifying
    @Query(value = """
            INSERT INTO carts (user_id, guest_token, status)
            VALUES (:userId, NULL, 'ACTIVE')
            ON CONFLICT (user_id) WHERE status = 'ACTIVE' AND user_id IS NOT NULL DO NOTHING
            """, nativeQuery = true)
    void insertActiveUserCartIfAbsent(@Param("userId") UUID userId);

    @Modifying
    @Query(value = """
            INSERT INTO carts (user_id, guest_token, status)
            VALUES (NULL, :guestToken, 'ACTIVE')
            ON CONFLICT (guest_token) WHERE status = 'ACTIVE' AND guest_token IS NOT NULL DO NOTHING
            """, nativeQuery = true)
    void insertActiveGuestCartIfAbsent(@Param("guestToken") UUID guestToken);
}
