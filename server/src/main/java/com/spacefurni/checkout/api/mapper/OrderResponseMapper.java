package com.spacefurni.checkout.api.mapper;

import com.spacefurni.checkout.api.dto.OrderResponse;
import com.spacefurni.checkout.api.dto.OrderSummaryResponse;
import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderResponseMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getId(), order.getOrderNumber(), order.getStatus(),
                order.getSubtotal().amount(), order.getShipping().amount(), order.getDiscount().amount(),
                order.getTotal().amount(), order.getTotal().currencyCode(), order.getPromotionCode(),
                toDeliveryDetailsResponse(order.getDeliveryDetails()), order.getDeliveryWindow(),
                order.getPaymentMethod(), order.getPaymentStatus(), order.getPlacedAt(),
                order.getItems().stream().map(this::toItemResponse).toList());
    }

    public OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(order.getId(), order.getOrderNumber(), order.getStatus(),
                order.getTotal().amount(), order.getTotal().currencyCode(), order.getItems().size(),
                order.getPlacedAt());
    }

    private OrderResponse.DeliveryDetailsResponse toDeliveryDetailsResponse(DeliveryDetails deliveryDetails) {
        return new OrderResponse.DeliveryDetailsResponse(deliveryDetails.getFullName(), deliveryDetails.getPhone(),
                deliveryDetails.getStreet(), deliveryDetails.getDistrict(), deliveryDetails.getCity(),
                deliveryDetails.getNote());
    }

    private OrderResponse.OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderResponse.OrderItemResponse(item.getProductId(), item.getProductNameSnapshot(),
                item.getSkuSnapshot(), item.getUnitPriceAmount(), item.getQuantity(), item.getLineTotalAmount());
    }
}
