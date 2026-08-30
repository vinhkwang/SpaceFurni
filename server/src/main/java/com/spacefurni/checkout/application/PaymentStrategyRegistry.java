package com.spacefurni.checkout.application;

import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.PaymentStrategy;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PaymentStrategyRegistry {

    private final Map<PaymentMethod, PaymentStrategy> strategiesByMethod;

    public PaymentStrategyRegistry(List<PaymentStrategy> strategies) {
        this.strategiesByMethod = indexByMethodFailingFastOnDuplicates(strategies);
        failFastIfAnyMethodIsUncovered();
    }

    public PaymentStrategy resolve(PaymentMethod method) {
        return strategiesByMethod.get(method);
    }

    private Map<PaymentMethod, PaymentStrategy> indexByMethodFailingFastOnDuplicates(
            List<PaymentStrategy> strategies) {
        Map<PaymentMethod, PaymentStrategy> index = new EnumMap<>(PaymentMethod.class);
        for (PaymentStrategy strategy : strategies) {
            PaymentMethod method = strategy.supportedMethod();
            PaymentStrategy existing = index.putIfAbsent(method, strategy);
            if (existing != null) {
                throw new IllegalStateException("Multiple PaymentStrategy implementations registered for " + method);
            }
        }
        return index;
    }

    private void failFastIfAnyMethodIsUncovered() {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (!strategiesByMethod.containsKey(method)) {
                throw new IllegalStateException("No PaymentStrategy registered for " + method);
            }
        }
    }
}
