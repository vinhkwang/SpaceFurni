package com.spacefurni.cart.application;

import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartItem;
import com.spacefurni.cart.domain.CartStatus;
import com.spacefurni.cart.infrastructure.CartRepository;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.inventory.domain.InsufficientStockException;
import com.spacefurni.shared.exception.BusinessRuleViolationException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final InventoryService inventoryService;

    public CartService(CartRepository cartRepository, InventoryService inventoryService) {
        this.cartRepository = cartRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public Cart resolveOrCreateActiveCart(UUID userId, UUID guestToken) {
        if (userId != null) {
            return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                    .orElseGet(() -> cartRepository.save(new Cart(userId, null)));
        }
        return cartRepository.findByGuestTokenAndStatus(guestToken, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(new Cart(null, guestToken)));
    }

    @Transactional
    public Cart addLine(Cart cart, UUID productId, int quantity) {
        requireQuantityAtLeastOne(quantity);
        int existingQuantity = cart.findLineByProductId(productId).map(CartItem::getQuantity).orElse(0);
        rejectIfExceedsAvailableStock(productId, existingQuantity + quantity);
        cart.addOrIncrementLine(productId, quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateLineQuantity(Cart cart, UUID productId, int quantity) {
        if (quantity == 0) {
            return removeLine(cart, productId);
        }
        requireQuantityAtLeastOne(quantity);
        rejectIfExceedsAvailableStock(productId, quantity);
        cart.setLineQuantity(productId, quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeLine(Cart cart, UUID productId) {
        cart.removeLine(productId);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart clear(Cart cart) {
        cart.clearLines();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart applyPromotion(Cart cart, String promotionCode) {
        cart.applyPromotion(promotionCode.toUpperCase());
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart clearPromotion(Cart cart) {
        cart.clearPromotion();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart markConverted(Cart cart) {
        cart.markConverted();
        return cartRepository.save(cart);
    }

    private void requireQuantityAtLeastOne(int quantity) {
        if (quantity < 1) {
            throw new BusinessRuleViolationException("Quantity must be at least 1");
        }
    }

    private void rejectIfExceedsAvailableStock(UUID productId, int requestedQuantity) {
        int availableQuantity = inventoryService.findAvailableQuantities(List.of(productId))
                .getOrDefault(productId, 0);
        if (requestedQuantity > availableQuantity) {
            throw new InsufficientStockException(productId, requestedQuantity, availableQuantity);
        }
    }
}
