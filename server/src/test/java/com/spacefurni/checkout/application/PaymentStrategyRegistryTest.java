package com.spacefurni.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.checkout.domain.BankTransferPaymentStrategy;
import com.spacefurni.checkout.domain.CardPaymentStrategy;
import com.spacefurni.checkout.domain.CashOnDeliveryPaymentStrategy;
import com.spacefurni.checkout.domain.EWalletPaymentStrategy;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.PaymentStrategy;
import java.util.List;
import org.junit.jupiter.api.Test;

class PaymentStrategyRegistryTest {

    @Test
    void resolvesEachStrategyByItsSupportedMethod() {
        CardPaymentStrategy cardPaymentStrategy = new CardPaymentStrategy();
        CashOnDeliveryPaymentStrategy cashOnDeliveryPaymentStrategy = new CashOnDeliveryPaymentStrategy();
        BankTransferPaymentStrategy bankTransferPaymentStrategy = new BankTransferPaymentStrategy();
        EWalletPaymentStrategy eWalletPaymentStrategy = new EWalletPaymentStrategy();

        PaymentStrategyRegistry registry = new PaymentStrategyRegistry(List.of(cardPaymentStrategy,
                cashOnDeliveryPaymentStrategy, bankTransferPaymentStrategy, eWalletPaymentStrategy));

        assertThat(registry.resolve(PaymentMethod.CARD)).isSameAs(cardPaymentStrategy);
        assertThat(registry.resolve(PaymentMethod.CASH_ON_DELIVERY)).isSameAs(cashOnDeliveryPaymentStrategy);
        assertThat(registry.resolve(PaymentMethod.BANK_TRANSFER)).isSameAs(bankTransferPaymentStrategy);
        assertThat(registry.resolve(PaymentMethod.E_WALLET)).isSameAs(eWalletPaymentStrategy);
    }

    @Test
    void failsFastAtConstructionWhenAMethodHasNoStrategy() {
        List<PaymentStrategy> incomplete = List.of(new CardPaymentStrategy(),
                new CashOnDeliveryPaymentStrategy());

        assertThatThrownBy(() -> new PaymentStrategyRegistry(incomplete)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void failsFastAtConstructionWhenTwoStrategiesShareAMethod() {
        List<PaymentStrategy> duplicated = List.of(new CardPaymentStrategy(),
                new CardPaymentStrategy(), new CashOnDeliveryPaymentStrategy(), new BankTransferPaymentStrategy());

        assertThatThrownBy(() -> new PaymentStrategyRegistry(duplicated)).isInstanceOf(IllegalStateException.class);
    }
}
