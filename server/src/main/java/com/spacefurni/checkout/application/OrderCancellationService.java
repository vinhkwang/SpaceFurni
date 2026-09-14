package com.spacefurni.checkout.application;

import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.inventory.api.dto.StockReservationLine;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.shared.domain.OrderStatusChangedEvent;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCancellationService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderCancellationService(OrderRepository orderRepository, InventoryService inventoryService,
            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order cancelOrder(UUID userId, UUID orderId, String reason) {
        Order order = findOwnedOrderByIdOrThrow(userId, orderId);
        if (!order.isCancellableByCustomer()) {
            throw new OrderNotCancellableException(order.getStatus());
        }
        OrderStatus previousStatus = order.getStatus();
        order.transitionTo(OrderStatus.CANCELLED);
        order.recordCancellationReason(reason);
        inventoryService.releaseStockForOrderLines(toStockReservationLines(order));
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), order.getOrderNumber(),
                order.getUserId(), previousStatus.name(), OrderStatus.CANCELLED.name()));
        return order;
    }

    private Order findOwnedOrderByIdOrThrow(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
        return order;
    }

    private List<StockReservationLine> toStockReservationLines(Order order) {
        return order.getItems().stream()
                .map(item -> new StockReservationLine(item.getProductId(), item.getQuantity()))
                .toList();
    }
}
