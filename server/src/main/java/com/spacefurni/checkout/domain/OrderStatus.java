package com.spacefurni.checkout.domain;

public enum OrderStatus {
    PENDING,
    PAID,
    PACKING,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == PAID || target == PACKING || target == CANCELLED;
            case PAID -> target == PACKING || target == CANCELLED;
            case PACKING -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
