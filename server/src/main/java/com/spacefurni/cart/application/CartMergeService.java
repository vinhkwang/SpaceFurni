package com.spacefurni.cart.application;

import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartItem;
import com.spacefurni.cart.domain.CartStatus;
import com.spacefurni.cart.infrastructure.CartRepository;
import com.spacefurni.inventory.application.InventoryService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartMergeService {

    private final CartRepository cartRepository;
    private final InventoryService inventoryService;

    public CartMergeService(CartRepository cartRepository, InventoryService inventoryService) {
        this.cartRepository = cartRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public void mergeGuestCartIntoUserCart(UUID guestToken, UUID userId) {
        if (guestToken == null) {
            return;
        }
        cartRepository.findByGuestTokenAndStatus(guestToken, CartStatus.ACTIVE).ifPresent(guestCart -> {
            Cart userCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                    .orElseGet(() -> cartRepository.save(new Cart(userId, null)));
            mergeLines(guestCart, userCart);
            guestCart.markConverted();
            cartRepository.save(userCart);
            cartRepository.save(guestCart);
        });
    }

    private void mergeLines(Cart guestCart, Cart userCart) {
        List<UUID> guestProductIds = guestCart.getItems().stream().map(CartItem::getProductId).toList();
        Map<UUID, Integer> availableQuantities = inventoryService.findAvailableQuantities(guestProductIds);
        for (CartItem guestLine : guestCart.getItems()) {
            UUID productId = guestLine.getProductId();
            int existingQuantity = userCart.findLineByProductId(productId).map(CartItem::getQuantity).orElse(0);
            int mergedQuantity = existingQuantity + guestLine.getQuantity();
            int availableQuantity = availableQuantities.getOrDefault(productId, 0);
            int cappedQuantity = Math.min(mergedQuantity, availableQuantity);
            if (cappedQuantity <= 0) {
                continue;
            }
            if (userCart.findLineByProductId(productId).isPresent()) {
                userCart.setLineQuantity(productId, cappedQuantity);
            } else {
                userCart.addOrIncrementLine(productId, cappedQuantity, guestLine.getColorHexCode());
            }
        }
    }
}
