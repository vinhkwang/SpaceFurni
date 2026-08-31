package com.spacefurni.checkout.application;

import com.spacefurni.checkout.api.dto.OrderTimelineStepResponse;
import com.spacefurni.checkout.domain.OrderStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderTimelineBuilder {

    public List<OrderTimelineStepResponse> build(OrderStatus status, Instant placedAt) {
        int completedRank = completedRankFor(status);
        return List.of(new OrderTimelineStepResponse("Order placed", placedAt.toString(), completedRank >= 1),
                new OrderTimelineStepResponse("Payment confirmed", null, completedRank >= 2),
                new OrderTimelineStepResponse("Packed", null, completedRank >= 3),
                new OrderTimelineStepResponse("Out for delivery", null, completedRank >= 4),
                new OrderTimelineStepResponse("Delivered", null, completedRank >= 4));
    }

    private int completedRankFor(OrderStatus status) {
        return switch (status) {
            case PENDING, CANCELLED -> 1;
            case PAID -> 2;
            case PACKING -> 3;
            case DELIVERED -> 4;
        };
    }
}
