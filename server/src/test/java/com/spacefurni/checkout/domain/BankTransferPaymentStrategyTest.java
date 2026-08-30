package com.spacefurni.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.domain.Money;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BankTransferPaymentStrategyTest {

    private final BankTransferPaymentStrategy strategy = new BankTransferPaymentStrategy();

    @Test
    void isPendingWithAGeneratedTransferReference() {
        DeliveryDetails deliveryDetails = new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1",
                "Ho Chi Minh City", null);
        Order order = new Order("SF-2419", UUID.randomUUID(), Money.ofVnd(1_000_000L), Money.ofVnd(300_000L),
                Money.zeroVnd(), Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.BANK_TRANSFER);

        PaymentResult result = strategy.execute(order);

        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.providerReference()).isNotBlank();
        assertThat(result.failureReason()).isNull();
    }

    @Test
    void supportsBankTransferMethod() {
        assertThat(strategy.supportedMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
    }
}
