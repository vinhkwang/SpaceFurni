package com.spacefurni.cart.infrastructure;

import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByUserIdAndStatus(UUID userId, CartStatus status);

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByGuestTokenAndStatus(UUID guestToken, CartStatus status);
}
