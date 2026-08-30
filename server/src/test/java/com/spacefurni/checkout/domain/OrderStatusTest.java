package com.spacefurni.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void pendingCanTransitionToPaidPackingOrCancelled() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PACKING)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PENDING)).isFalse();
    }

    @Test
    void paidCanTransitionToPackingOrCancelled() {
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PACKING)).isTrue();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PAID)).isFalse();
    }

    @Test
    void packingCanOnlyTransitionToDelivered() {
        assertThat(OrderStatus.PACKING.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
        assertThat(OrderStatus.PACKING.canTransitionTo(OrderStatus.PAID)).isFalse();
        assertThat(OrderStatus.PACKING.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.PACKING.canTransitionTo(OrderStatus.PENDING)).isFalse();
    }

    @Test
    void deliveredIsTerminal() {
        for (OrderStatus target : OrderStatus.values()) {
            assertThat(OrderStatus.DELIVERED.canTransitionTo(target)).isFalse();
        }
    }

    @Test
    void cancelledIsTerminal() {
        for (OrderStatus target : OrderStatus.values()) {
            assertThat(OrderStatus.CANCELLED.canTransitionTo(target)).isFalse();
        }
    }
}
