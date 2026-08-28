package com.spacefurni.pricing.application;

import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.pricing.domain.NextDayShippingFeeStrategy;
import com.spacefurni.pricing.domain.ShippingFeeStrategy;
import com.spacefurni.pricing.domain.StandardShippingFeeStrategy;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ShippingFeeStrategyResolver {

    private final Map<DeliveryWindow, ShippingFeeStrategy> strategiesByDeliveryWindow;

    public ShippingFeeStrategyResolver(StandardShippingFeeStrategy standardShippingFeeStrategy,
            NextDayShippingFeeStrategy nextDayShippingFeeStrategy) {
        this.strategiesByDeliveryWindow = Map.of(DeliveryWindow.STANDARD, standardShippingFeeStrategy,
                DeliveryWindow.NEXT_DAY, nextDayShippingFeeStrategy);
    }

    public ShippingFeeStrategy resolve(DeliveryWindow deliveryWindow) {
        return strategiesByDeliveryWindow.get(deliveryWindow);
    }
}
