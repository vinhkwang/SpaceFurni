package com.spacefurni.checkout.application;

import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.RefundStrategy;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RefundStrategyRegistry {

    private final Map<PaymentMethod, RefundStrategy> strategiesByMethod;

    public RefundStrategyRegistry(List<RefundStrategy> strategies) {
        this.strategiesByMethod = indexByMethodFailingFastOnDuplicates(strategies);
        failFastIfAnyMethodIsUncovered();
    }

    public RefundStrategy resolve(PaymentMethod method) {
        return strategiesByMethod.get(method);
    }

    private Map<PaymentMethod, RefundStrategy> indexByMethodFailingFastOnDuplicates(List<RefundStrategy> strategies) {
        Map<PaymentMethod, RefundStrategy> index = new EnumMap<>(PaymentMethod.class);
        for (RefundStrategy strategy : strategies) {
            for (PaymentMethod method : strategy.supportedMethods()) {
                RefundStrategy existing = index.putIfAbsent(method, strategy);
                if (existing != null) {
                    throw new IllegalStateException("Multiple RefundStrategy implementations registered for " + method);
                }
            }
        }
        return index;
    }

    private void failFastIfAnyMethodIsUncovered() {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (!strategiesByMethod.containsKey(method)) {
                throw new IllegalStateException("No RefundStrategy registered for " + method);
            }
        }
    }
}
