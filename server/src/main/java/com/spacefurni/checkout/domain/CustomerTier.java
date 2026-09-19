package com.spacefurni.checkout.domain;

public enum CustomerTier {
    NEW,
    RETURNING,
    VIP;

    public static CustomerTier fromOrderCount(long orderCount) {
        if (orderCount >= 3) {
            return VIP;
        }
        if (orderCount == 2) {
            return RETURNING;
        }
        return NEW;
    }

    public int minimumOrderCount() {
        return switch (this) {
            case NEW -> 1;
            case RETURNING -> 2;
            case VIP -> 3;
        };
    }

    public Integer maximumOrderCount() {
        return switch (this) {
            case NEW -> 1;
            case RETURNING -> 2;
            case VIP -> null;
        };
    }
}
