package com.spacefurni.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomerTierTest {

    @Test
    void oneOrderIsNew() {
        assertThat(CustomerTier.fromOrderCount(0)).isEqualTo(CustomerTier.NEW);
        assertThat(CustomerTier.fromOrderCount(1)).isEqualTo(CustomerTier.NEW);
    }

    @Test
    void twoOrdersIsReturning() {
        assertThat(CustomerTier.fromOrderCount(2)).isEqualTo(CustomerTier.RETURNING);
    }

    @Test
    void threeOrMoreOrdersIsVip() {
        assertThat(CustomerTier.fromOrderCount(3)).isEqualTo(CustomerTier.VIP);
        assertThat(CustomerTier.fromOrderCount(12)).isEqualTo(CustomerTier.VIP);
    }

    @Test
    void minimumAndMaximumOrderCountsBoundEachTier() {
        assertThat(CustomerTier.NEW.minimumOrderCount()).isEqualTo(1);
        assertThat(CustomerTier.NEW.maximumOrderCount()).isEqualTo(1);
        assertThat(CustomerTier.RETURNING.minimumOrderCount()).isEqualTo(2);
        assertThat(CustomerTier.RETURNING.maximumOrderCount()).isEqualTo(2);
        assertThat(CustomerTier.VIP.minimumOrderCount()).isEqualTo(3);
        assertThat(CustomerTier.VIP.maximumOrderCount()).isNull();
    }
}
