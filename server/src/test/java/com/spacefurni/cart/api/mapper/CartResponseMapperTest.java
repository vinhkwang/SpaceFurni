package com.spacefurni.cart.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.cart.api.dto.CartResponse;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CartResponseMapperTest {

    private final CartResponseMapper mapper = new CartResponseMapper();

    private ProductSummaryResponse summaryOf(UUID productId, long priceAmount) {
        return new ProductSummaryResponse(productId, "slug-" + productId, "Product " + productId, "Category",
                priceAmount, null, "VND", null, 0, "https://example.com/" + productId + ".jpg", null);
    }

    @Test
    void toResponseComputesLineTotalsAndSubtotalFromLivePrices() {
        UUID guestToken = UUID.randomUUID();
        Cart cart = new Cart(null, guestToken);
        UUID firstProductId = UUID.randomUUID();
        UUID secondProductId = UUID.randomUUID();
        cart.addOrIncrementLine(firstProductId, 2);
        cart.addOrIncrementLine(secondProductId, 3);
        Map<UUID, ProductSummaryResponse> products = Map.of(firstProductId, summaryOf(firstProductId, 100_000L),
                secondProductId, summaryOf(secondProductId, 50_000L));

        CartResponse response = mapper.toResponse(cart, products);

        assertThat(response.guestToken()).isEqualTo(guestToken);
        assertThat(response.currencyCode()).isEqualTo("VND");
        assertThat(response.lines()).hasSize(2);
        assertThat(response.lines()).filteredOn(line -> line.productId().equals(firstProductId)).first()
                .satisfies(line -> {
                    assertThat(line.unitPriceAmount()).isEqualTo(100_000L);
                    assertThat(line.quantity()).isEqualTo(2);
                    assertThat(line.lineTotalAmount()).isEqualTo(200_000L);
                });
        assertThat(response.subtotalAmount()).isEqualTo(350_000L);
    }

    @Test
    void toResponseReturnsEmptyLinesAndZeroSubtotalForEmptyCart() {
        Cart cart = new Cart(null, UUID.randomUUID());

        CartResponse response = mapper.toResponse(cart, Map.of());

        assertThat(response.lines()).isEmpty();
        assertThat(response.subtotalAmount()).isZero();
        assertThat(response.currencyCode()).isEqualTo("VND");
    }
}
