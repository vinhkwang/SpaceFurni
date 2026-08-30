package com.spacefurni.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.domain.Money;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CardPaymentStrategyTest {

    private final CardPaymentStrategy strategy = new CardPaymentStrategy();

    private Order orderWithPhone(String phone) {
        DeliveryDetails deliveryDetails = new DeliveryDetails("Nguyen Van A", phone, "1 Le Loi", "District 1",
                "Ho Chi Minh City", null);
        return new Order("SF-2419", UUID.randomUUID(), Money.ofVnd(1_000_000L), Money.ofVnd(300_000L),
                Money.zeroVnd(), Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CARD);
    }

    @Test
    void capturesWithAGeneratedReferenceForAnOrdinaryOrder() {
        PaymentResult result = strategy.execute(orderWithPhone("0901234567"));

        assertThat(result.status()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(result.providerReference()).isNotBlank();
        assertThat(result.failureReason()).isNull();
    }

    @Test
    void failsForTheDocumentedDeclinedTestPhoneNumber() {
        PaymentResult result = strategy.execute(orderWithPhone(CardPaymentStrategy.DECLINED_TEST_PHONE_NUMBER));

        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.providerReference()).isNull();
        assertThat(result.failureReason()).isNotBlank();
    }

    @Test
    void supportsCardMethod() {
        assertThat(strategy.supportedMethod()).isEqualTo(PaymentMethod.CARD);
    }
}
