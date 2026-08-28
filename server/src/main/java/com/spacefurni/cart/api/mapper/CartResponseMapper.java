package com.spacefurni.cart.api.mapper;

import com.spacefurni.cart.api.dto.CartLineResponse;
import com.spacefurni.cart.api.dto.CartResponse;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartItem;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.shared.domain.Money;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartResponseMapper {

    public CartResponse toResponse(Cart cart, Map<UUID, ProductSummaryResponse> productSummariesByProductId) {
        List<CartLineResponse> lines = cart.getItems().stream()
                .map(item -> toLine(item, productSummariesByProductId.get(item.getProductId()))).toList();
        String currencyCode = lines.isEmpty() ? "VND" : lines.get(0).currencyCode();
        long subtotalAmount = lines.stream().mapToLong(CartLineResponse::lineTotalAmount).sum();
        return new CartResponse(cart.getId(), cart.getGuestToken(), lines, subtotalAmount, currencyCode);
    }

    private CartLineResponse toLine(CartItem item, ProductSummaryResponse product) {
        Money lineTotal = new Money(product.priceAmount(), product.currencyCode()).multipliedBy(item.getQuantity());
        return new CartLineResponse(product.id(), product.slug(), product.name(), product.primaryImageUrl(),
                product.priceAmount(), product.currencyCode(), item.getQuantity(), lineTotal.amount());
    }
}
