package com.spacefurni.cart.infrastructure;

import com.spacefurni.cart.domain.CartItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
}
