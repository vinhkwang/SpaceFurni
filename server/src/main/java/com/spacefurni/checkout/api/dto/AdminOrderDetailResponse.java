package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.PaymentStatus;
import java.time.Instant;
import java.util.List;

public record AdminOrderDetailResponse(String orderNumber, OrderStatus status, CustomerResponse customer,
        DeliveryAddressResponse deliveryAddress, DeliveryWindow deliveryWindow, PaymentMethod paymentMethod,
        PaymentStatus paymentStatus, long subtotalAmount, long shippingAmount, long discountAmount,
        long totalAmount, String currencyCode, Instant placedAt, List<OrderLineResponse> lines,
        List<OrderTimelineStepResponse> timeline) {

    public record CustomerResponse(String fullName, String email, String phone) {
    }

    public record DeliveryAddressResponse(String street, String district, String city, String note) {
    }

    public record OrderLineResponse(String productName, long unitPriceAmount, int quantity, long lineTotalAmount) {
    }
}
