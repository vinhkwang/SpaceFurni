package com.spacefurni.checkout.application;

import com.spacefurni.checkout.api.dto.AdminCustomerDetailResponse;
import com.spacefurni.checkout.api.dto.AdminCustomerOrderSummaryResponse;
import com.spacefurni.checkout.api.dto.AdminCustomerRowResponse;
import com.spacefurni.checkout.domain.CustomerTier;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.checkout.infrastructure.OrderRepository.CustomerAggregateRow;
import com.spacefurni.checkout.infrastructure.OrderRepository.CustomerTierCount;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCustomerQueryService {

    private static final String CSV_HEADER_ROW =
            "Full name,Email,District,Orders,Lifetime value,Currency,First order,Tier";

    private static final int RECENT_ORDERS_LIMIT = 10;

    private final OrderRepository orderRepository;

    public AdminCustomerQueryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminCustomerRowResponse> listCustomers(CustomerTier tier, String searchTerm, Pageable pageable) {
        String searchPattern = toSearchPattern(searchTerm);
        Page<CustomerAggregateRow> aggregates = orderRepository.findCustomerAggregates(searchPattern,
                minimumOrderCount(tier), maximumOrderCount(tier), pageable);
        return aggregates.map(this::toRow);
    }

    @Transactional(readOnly = true)
    public Map<CustomerTier, Long> countCustomersByTier() {
        Map<CustomerTier, Long> counts = new EnumMap<>(CustomerTier.class);
        for (CustomerTierCount tierCount : orderRepository.countGroupedByTier()) {
            counts.put(CustomerTier.valueOf(tierCount.getTier()), tierCount.getTotal());
        }
        return counts;
    }

    @Transactional(readOnly = true)
    public AdminCustomerDetailResponse findCustomerDetail(UUID customerId) {
        CustomerAggregateRow aggregate = orderRepository.findCustomerAggregateById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        List<AdminCustomerOrderSummaryResponse> recentOrders = orderRepository
                .findAllByUserIdOrderByPlacedAtDesc(customerId, PageRequest.of(0, RECENT_ORDERS_LIMIT)).stream()
                .map(this::toOrderSummary).toList();
        return new AdminCustomerDetailResponse(aggregate.getId(), aggregate.getFullName(), aggregate.getEmail(),
                aggregate.getDistrict(), aggregate.getOrderCount(), aggregate.getLifetimeValueAmount(),
                aggregate.getCurrencyCode(), aggregate.getFirstOrderAt(),
                CustomerTier.fromOrderCount(aggregate.getOrderCount()), recentOrders);
    }

    @Transactional(readOnly = true)
    public byte[] exportCustomersCsv(CustomerTier tier, String searchTerm) {
        String searchPattern = toSearchPattern(searchTerm);
        List<AdminCustomerRowResponse> rows = orderRepository
                .findAllCustomerAggregates(searchPattern, minimumOrderCount(tier), maximumOrderCount(tier)).stream()
                .map(this::toRow).toList();
        return formatAsCsv(rows).getBytes(StandardCharsets.UTF_8);
    }

    private String toSearchPattern(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return null;
        }
        return "%" + searchTerm.trim().toLowerCase() + "%";
    }

    private Integer minimumOrderCount(CustomerTier tier) {
        return tier == null ? null : tier.minimumOrderCount();
    }

    private Integer maximumOrderCount(CustomerTier tier) {
        return tier == null ? null : tier.maximumOrderCount();
    }

    private AdminCustomerRowResponse toRow(CustomerAggregateRow row) {
        return new AdminCustomerRowResponse(row.getId(), row.getFullName(), row.getEmail(), row.getDistrict(),
                row.getOrderCount(), row.getLifetimeValueAmount(), row.getCurrencyCode(), row.getFirstOrderAt(),
                CustomerTier.fromOrderCount(row.getOrderCount()));
    }

    private AdminCustomerOrderSummaryResponse toOrderSummary(Order order) {
        return new AdminCustomerOrderSummaryResponse(order.getOrderNumber(), order.getPlacedAt(),
                order.getTotal().amount(), order.getTotal().currencyCode(), order.getStatus());
    }

    private String formatAsCsv(List<AdminCustomerRowResponse> rows) {
        StringBuilder csv = new StringBuilder(CSV_HEADER_ROW).append("\r\n");
        for (AdminCustomerRowResponse row : rows) {
            csv.append(escapeCsvField(row.fullName())).append(',').append(escapeCsvField(row.email())).append(',')
                    .append(escapeCsvField(row.district())).append(',').append(row.orderCount()).append(',')
                    .append(row.lifetimeValueAmount()).append(',').append(escapeCsvField(row.currencyCode()))
                    .append(',').append(row.firstOrderAt()).append(',').append(row.tier()).append("\r\n");
        }
        return csv.toString();
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        boolean needsQuoting = field.contains(",") || field.contains("\"") || field.contains("\n");
        String escaped = field.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }
}
