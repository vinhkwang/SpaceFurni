package com.spacefurni.cart.api.mapper;

import com.spacefurni.cart.api.dto.CartLineResponse;
import com.spacefurni.cart.api.dto.CartResponse;
import com.spacefurni.cart.api.dto.PriceBreakdownResponse;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartItem;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.pricing.domain.PriceBreakdown;
import com.spacefurni.shared.domain.Money;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartResponseMapper {

    public CartResponse toResponse(Cart cart, Map<UUID, ProductSummaryResponse> productSummariesByProductId,
            PriceBreakdown priceBreakdown) {
        List<CartLineResponse> lines = cart.getItems().stream()
                .map(item -> toLine(item, productSummariesByProductId.get(item.getProductId()))).toList();
        return new CartResponse(cart.getId(), cart.getGuestToken(), lines, toPriceBreakdownResponse(priceBreakdown));
    }

    public PriceBreakdownResponse toPriceBreakdownResponse(PriceBreakdown priceBreakdown) {
        return new PriceBreakdownResponse(priceBreakdown.subtotal().amount(), priceBreakdown.shipping().amount(),
                priceBreakdown.discount().amount(), priceBreakdown.total().amount(),
                priceBreakdown.subtotal().currencyCode(), priceBreakdown.appliedPromotionCode(),
                priceBreakdown.amountToFreeShipping().amount());
    }

    private CartLineResponse toLine(CartItem item, ProductSummaryResponse product) {
        Money lineTotal = new Money(product.priceAmount(), product.currencyCode()).multipliedBy(item.getQuantity());
        return new CartLineResponse(product.id(), product.slug(), product.name(), product.primaryImageUrl(),
                product.priceAmount(), product.currencyCode(), item.getQuantity(), lineTotal.amount(),
                item.getColorHexCode(), resolveColorName(item, product));
    }

    private String resolveColorName(CartItem item, ProductSummaryResponse product) {
        if (item.getColorHexCode() == null || product.primaryColorHexCode() == null) {
            return null;
        }
        return item.getColorHexCode().equalsIgnoreCase(product.primaryColorHexCode()) ? product.primaryColorName()
                : null;
    }
}
