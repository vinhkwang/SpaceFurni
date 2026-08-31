package com.spacefurni.checkout.application;

import com.spacefurni.checkout.api.dto.AdminOrderRowResponse;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderItemRepository;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.checkout.infrastructure.OrderRepository.OrderStatusCount;
import com.spacefurni.checkout.infrastructure.OrderSearchSpecifications;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminOrderQueryService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminOrderRowResponse> listOrders(OrderStatus status, String searchTerm, Pageable pageable) {
        Specification<Order> specification = Specification.unrestricted();
        if (status != null) {
            specification = specification.and(OrderSearchSpecifications.hasStatus(status));
        }
        if (searchTerm != null && !searchTerm.isBlank()) {
            specification =
                    specification.and(OrderSearchSpecifications.orderNumberOrCustomerNameContains(searchTerm));
        }
        Page<Order> orders = orderRepository.findAll(specification, pageable);
        Map<UUID, List<OrderItem>> itemsByOrderId = findItemsGroupedByOrderId(orders.getContent());
        return orders.map(order -> toRow(order, itemsByOrderId.getOrDefault(order.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public Map<OrderStatus, Long> countOrdersByStatus() {
        return orderRepository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(OrderStatusCount::getStatus, OrderStatusCount::getTotal));
    }

    private Map<UUID, List<OrderItem>> findItemsGroupedByOrderId(List<Order> orders) {
        List<UUID> orderIds = orders.stream().map(Order::getId).toList();
        return orderItemRepository.findAllByOrder_IdIn(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));
    }

    private AdminOrderRowResponse toRow(Order order, List<OrderItem> items) {
        return new AdminOrderRowResponse(order.getOrderNumber(), order.getDeliveryDetails().getFullName(),
                order.getDeliveryDetails().getDistrict(), buildItemSummary(items), items.size(),
                formatPaymentLabel(order.getPaymentMethod()), order.getPlacedAt(), order.getTotal().amount(),
                order.getTotal().currencyCode(), order.getStatus());
    }

    private String buildItemSummary(List<OrderItem> items) {
        if (items.isEmpty()) {
            return "";
        }
        String firstItemName = items.get(0).getProductNameSnapshot();
        int remainingItemCount = items.size() - 1;
        return remainingItemCount > 0 ? firstItemName + " +" + remainingItemCount + " more" : firstItemName;
    }

    private String formatPaymentLabel(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case CARD -> "Card";
            case CASH_ON_DELIVERY -> "Cash on delivery";
            case BANK_TRANSFER -> "Bank transfer";
        };
    }
}
