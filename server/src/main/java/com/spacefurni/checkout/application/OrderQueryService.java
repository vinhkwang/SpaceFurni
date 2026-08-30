package com.spacefurni.checkout.application;

import com.spacefurni.checkout.api.dto.OrderResponse;
import com.spacefurni.checkout.api.dto.OrderSummaryResponse;
import com.spacefurni.checkout.api.mapper.OrderResponseMapper;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderResponseMapper orderResponseMapper;

    public OrderQueryService(OrderRepository orderRepository, OrderResponseMapper orderResponseMapper) {
        this.orderRepository = orderRepository;
        this.orderResponseMapper = orderResponseMapper;
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> findOrderHistory(UUID userId, Pageable pageable) {
        return orderRepository.findAllByUserIdOrderByPlacedAtDesc(userId, pageable)
                .map(orderResponseMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrderDetail(UUID userId, String orderNumber) {
        return orderResponseMapper.toResponse(findOwnedOrderByOrderNumberOrThrow(userId, orderNumber));
    }

    private Order findOwnedOrderByOrderNumberOrThrow(UUID userId, String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found: " + orderNumber);
        }
        return order;
    }
}
