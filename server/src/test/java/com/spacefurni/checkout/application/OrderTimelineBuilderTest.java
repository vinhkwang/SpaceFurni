package com.spacefurni.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.checkout.api.dto.OrderTimelineStepResponse;
import com.spacefurni.checkout.domain.OrderStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderTimelineBuilderTest {

    private final OrderTimelineBuilder orderTimelineBuilder = new OrderTimelineBuilder();

    @Test
    void marksOnlyOrderPlacedCompleteWhenPending() {
        assertThat(completionOf(OrderStatus.PENDING)).containsExactly(true, false, false, false, false);
    }

    @Test
    void marksPlacedAndPaymentConfirmedCompleteWhenPaid() {
        assertThat(completionOf(OrderStatus.PAID)).containsExactly(true, true, false, false, false);
    }

    @Test
    void marksThroughPackedCompleteWhenPacking() {
        assertThat(completionOf(OrderStatus.PACKING)).containsExactly(true, true, true, false, false);
    }

    @Test
    void marksAllStepsCompleteWhenDelivered() {
        assertThat(completionOf(OrderStatus.DELIVERED)).containsExactly(true, true, true, true, true);
    }

    @Test
    void marksOnlyOrderPlacedCompleteWhenCancelled() {
        assertThat(completionOf(OrderStatus.CANCELLED)).containsExactly(true, false, false, false, false);
    }

    @Test
    void usesTheFixedFiveStepLabelsInOrder() {
        List<OrderTimelineStepResponse> steps = orderTimelineBuilder.build(OrderStatus.PENDING, Instant.now());

        assertThat(steps).extracting(OrderTimelineStepResponse::label).containsExactly("Order placed",
                "Payment confirmed", "Packed", "Out for delivery", "Delivered");
    }

    @Test
    void carriesThePlacedAtTimestampOnTheFirstStepOnly() {
        Instant placedAt = Instant.parse("2026-08-10T09:24:00Z");

        List<OrderTimelineStepResponse> steps = orderTimelineBuilder.build(OrderStatus.PENDING, placedAt);

        assertThat(steps.get(0).detail()).isEqualTo(placedAt.toString());
        assertThat(steps.subList(1, 5)).extracting(OrderTimelineStepResponse::detail).containsOnly((String) null);
    }

    private List<Boolean> completionOf(OrderStatus status) {
        return orderTimelineBuilder.build(status, Instant.now()).stream().map(OrderTimelineStepResponse::complete)
                .toList();
    }
}
