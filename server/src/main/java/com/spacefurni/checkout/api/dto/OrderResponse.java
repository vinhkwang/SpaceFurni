package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.PaymentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, String orderNumber, OrderStatus status, long subtotalAmount,
        long shippingAmount, long discountAmount, long totalAmount, String currencyCode, String promotionCode,
        DeliveryDetailsResponse deliveryDetails, DeliveryWindow deliveryWindow, PaymentMethod paymentMethod,
        PaymentStatus paymentStatus, Instant placedAt, List<OrderItemResponse> items) {

    public record DeliveryDetailsResponse(String fullName, String phone, String street, String district,
            String city, String note) {
    }

    public record OrderItemResponse(UUID productId, String productName, String sku, long unitPriceAmount,
            int quantity, long lineTotalAmount) {
    }
}
