package com.spacefurni.checkout.application;

import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.inventory.api.dto.StockReservationLine;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    public AdminOrderService(OrderRepository orderRepository, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public void transitionOrderStatus(String orderNumber, OrderStatus targetStatus, Long version) {
        Order order = findOrderByOrderNumberOrThrow(orderNumber);
        if (!order.getVersion().equals(version)) {
            throw new OptimisticLockingFailureException("Order was modified by another request: " + orderNumber);
        }
        order.transitionTo(targetStatus);
        if (targetStatus == OrderStatus.CANCELLED) {
            releaseReservedStock(order);
        }
        orderRepository.save(order);
    }

    private void releaseReservedStock(Order order) {
        inventoryService.releaseStockForOrderLines(order.getItems().stream().map(this::toStockReservationLine)
                .toList());
    }

    private StockReservationLine toStockReservationLine(OrderItem item) {
        return new StockReservationLine(item.getProductId(), item.getQuantity());
    }

    private Order findOrderByOrderNumberOrThrow(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
    }
}
