package com.spacefurni.cart.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.cart.api.dto.CartResponse;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.pricing.domain.PriceBreakdown;
import com.spacefurni.shared.domain.Money;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CartResponseMapperTest {

    private final CartResponseMapper mapper = new CartResponseMapper();

    private ProductSummaryResponse summaryOf(UUID productId, long priceAmount) {
        return new ProductSummaryResponse(productId, "SKU-" + productId, "slug-" + productId,
                "Product " + productId, "Category", priceAmount, null, "VND", null, 0,
                "https://example.com/" + productId + ".jpg", null, "Beige", "#8B5E3C");
    }

    private PriceBreakdown priceBreakdownOf(long subtotalAmount) {
        return new PriceBreakdown(Money.ofVnd(subtotalAmount), Money.ofVnd(300_000L), Money.zeroVnd(),
                Money.ofVnd(subtotalAmount + 300_000L), null, Money.ofVnd(10_000_001L - subtotalAmount));
    }

    @Test
    void toResponseMapsLinesFromLivePricesAndFlattensThePriceBreakdown() {
        UUID guestToken = UUID.randomUUID();
        Cart cart = new Cart(null, guestToken);
        UUID firstProductId = UUID.randomUUID();
        UUID secondProductId = UUID.randomUUID();
        cart.addOrIncrementLine(firstProductId, 2, null);
        cart.addOrIncrementLine(secondProductId, 3, null);
        Map<UUID, ProductSummaryResponse> products = Map.of(firstProductId, summaryOf(firstProductId, 100_000L),
                secondProductId, summaryOf(secondProductId, 50_000L));

        CartResponse response = mapper.toResponse(cart, products, priceBreakdownOf(350_000L));

        assertThat(response.guestToken()).isEqualTo(guestToken);
        assertThat(response.lines()).hasSize(2);
        assertThat(response.lines()).filteredOn(line -> line.productId().equals(firstProductId)).first()
                .satisfies(line -> {
                    assertThat(line.unitPriceAmount()).isEqualTo(100_000L);
                    assertThat(line.quantity()).isEqualTo(2);
                    assertThat(line.lineTotalAmount()).isEqualTo(200_000L);
                });
        assertThat(response.priceBreakdown().subtotalAmount()).isEqualTo(350_000L);
        assertThat(response.priceBreakdown().currencyCode()).isEqualTo("VND");
    }

    @Test
    void toResponseResolvesColorNameOnlyWhenStoredHexMatchesThePrimarySwatch() {
        Cart cart = new Cart(null, UUID.randomUUID());
        UUID matchingProductId = UUID.randomUUID();
        UUID differingProductId = UUID.randomUUID();
        cart.addOrIncrementLine(matchingProductId, 1, "#8B5E3C");
        cart.addOrIncrementLine(differingProductId, 1, "#26241F");
        Map<UUID, ProductSummaryResponse> products = Map.of(matchingProductId, summaryOf(matchingProductId, 100_000L),
                differingProductId, summaryOf(differingProductId, 100_000L));

        CartResponse response = mapper.toResponse(cart, products, priceBreakdownOf(200_000L));

        assertThat(response.lines()).filteredOn(line -> line.productId().equals(matchingProductId)).first()
                .satisfies(line -> {
                    assertThat(line.colorHexCode()).isEqualTo("#8B5E3C");
                    assertThat(line.colorName()).isEqualTo("Beige");
                });
        assertThat(response.lines()).filteredOn(line -> line.productId().equals(differingProductId)).first()
                .satisfies(line -> {
                    assertThat(line.colorHexCode()).isEqualTo("#26241F");
                    assertThat(line.colorName()).isNull();
                });
    }

    @Test
    void toResponseReturnsEmptyLinesForEmptyCart() {
        Cart cart = new Cart(null, UUID.randomUUID());

        CartResponse response = mapper.toResponse(cart, Map.of(), priceBreakdownOf(0L));

        assertThat(response.lines()).isEmpty();
        assertThat(response.priceBreakdown().subtotalAmount()).isZero();
    }

    @Test
    void toPriceBreakdownResponseFlattensEveryField() {
        PriceBreakdown priceBreakdown = new PriceBreakdown(Money.ofVnd(2_000_000L), Money.ofVnd(300_000L),
                Money.ofVnd(200_000L), Money.ofVnd(2_100_000L), "SPACE10", Money.ofVnd(8_000_001L));

        var response = mapper.toPriceBreakdownResponse(priceBreakdown);

        assertThat(response.subtotalAmount()).isEqualTo(2_000_000L);
        assertThat(response.shippingAmount()).isEqualTo(300_000L);
        assertThat(response.discountAmount()).isEqualTo(200_000L);
        assertThat(response.totalAmount()).isEqualTo(2_100_000L);
        assertThat(response.currencyCode()).isEqualTo("VND");
        assertThat(response.appliedPromotionCode()).isEqualTo("SPACE10");
        assertThat(response.amountToFreeShippingAmount()).isEqualTo(8_000_001L);
    }
}
