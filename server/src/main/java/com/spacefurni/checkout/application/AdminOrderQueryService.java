package com.spacefurni.checkout.application;

import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.application.CatalogQueryService;
import com.spacefurni.checkout.api.dto.AdminOrderDetailResponse;
import com.spacefurni.checkout.api.dto.AdminOrderRowResponse;
import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderItemRepository;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.checkout.infrastructure.OrderRepository.OrderStatusCount;
import com.spacefurni.checkout.infrastructure.OrderSearchSpecifications;
import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final CurrentUserQueryService currentUserQueryService;
    private final OrderTimelineBuilder orderTimelineBuilder;
    private final CatalogQueryService catalogQueryService;

    public AdminOrderQueryService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            CurrentUserQueryService currentUserQueryService, OrderTimelineBuilder orderTimelineBuilder,
            CatalogQueryService catalogQueryService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.currentUserQueryService = currentUserQueryService;
        this.orderTimelineBuilder = orderTimelineBuilder;
        this.catalogQueryService = catalogQueryService;
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

    @Transactional(readOnly = true)
    public long countOrdersPlacedToday() {
        return orderRepository.countByPlacedAtGreaterThanEqual(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailResponse findOrderDetail(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        String customerEmail = currentUserQueryService.getEmailById(order.getUserId());
        return toDetail(order, customerEmail);
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

    private AdminOrderDetailResponse toDetail(Order order, String customerEmail) {
        DeliveryDetails deliveryDetails = order.getDeliveryDetails();
        Map<UUID, ProductSummaryResponse> productSummariesByProductId = catalogQueryService
                .findProductSummariesByIds(order.getItems().stream().map(OrderItem::getProductId).toList());
        return new AdminOrderDetailResponse(order.getOrderNumber(), order.getStatus(), order.getVersion(),
                new AdminOrderDetailResponse.CustomerResponse(deliveryDetails.getFullName(), customerEmail,
                        deliveryDetails.getPhone()),
                new AdminOrderDetailResponse.DeliveryAddressResponse(deliveryDetails.getStreet(),
                        deliveryDetails.getDistrict(), deliveryDetails.getCity(), deliveryDetails.getNote()),
                order.getDeliveryWindow(), order.getPaymentMethod(), order.getPaymentStatus(),
                order.getSubtotal().amount(), order.getShipping().amount(), order.getDiscount().amount(),
                order.getTotal().amount(), order.getTotal().currencyCode(), order.getPlacedAt(),
                order.getItems().stream().map(item -> toLine(item, productSummariesByProductId.get(item.getProductId())))
                        .toList(),
                orderTimelineBuilder.build(order.getStatus(), order.getPlacedAt()));
    }

    private AdminOrderDetailResponse.OrderLineResponse toLine(OrderItem item, ProductSummaryResponse product) {
        String imageUrl = product == null ? null : product.primaryImageUrl();
        return new AdminOrderDetailResponse.OrderLineResponse(item.getProductNameSnapshot(), imageUrl,
                item.getUnitPriceAmount(), item.getQuantity(), item.getLineTotalAmount());
    }
}
