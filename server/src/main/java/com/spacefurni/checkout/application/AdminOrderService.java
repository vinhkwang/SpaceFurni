package com.spacefurni.checkout.application;

import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentStatus;
import com.spacefurni.checkout.domain.RefundStrategy;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.inventory.api.dto.StockReservationLine;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.BusinessRuleViolationException;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final RefundStrategyRegistry refundStrategyRegistry;

    public AdminOrderService(OrderRepository orderRepository, InventoryService inventoryService,
            RefundStrategyRegistry refundStrategyRegistry) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.refundStrategyRegistry = refundStrategyRegistry;
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

    @Transactional
    public void processRefund(String orderNumber, Money amount) {
        Order order = findOrderByOrderNumberOrThrow(orderNumber);
        if (!isEligibleForRefund(order)) {
            throw new BusinessRuleViolationException("Order cannot be refunded while in status " + order.getStatus());
        }
        RefundStrategy refundStrategy = refundStrategyRegistry.resolve(order.getPaymentMethod());
        refundStrategy.refund(order, amount);
        order.recordPaymentStatus(PaymentStatus.REFUNDED);
        order.recordRefundedAmount(amount);
        orderRepository.save(order);
    }

    private boolean isEligibleForRefund(Order order) {
        return order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED;
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
