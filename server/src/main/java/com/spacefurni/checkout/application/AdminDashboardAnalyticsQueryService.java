package com.spacefurni.checkout.application;

import com.spacefurni.checkout.infrastructure.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardAnalyticsQueryService {

    private final OrderRepository orderRepository;

    public AdminDashboardAnalyticsQueryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<MonthlyRevenuePoint> monthlyRevenue(int months) {
        Instant sinceInclusive = firstDayOfEarliestIncludedMonth(months);
        return orderRepository.findMonthlyRevenueSince(sinceInclusive).stream()
                .map(row -> new MonthlyRevenuePoint(row.getMonth(), row.getRevenue())).toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentRevenueShare> revenueShareByDepartment() {
        return orderRepository.findRevenueShareByDepartment().stream()
                .map(row -> new DepartmentRevenueShare(row.getDepartment(), row.getRevenue(), row.getShare()))
                .toList();
    }

    private Instant firstDayOfEarliestIncludedMonth(int months) {
        return LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1).minusMonths(months - 1L).atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    public record MonthlyRevenuePoint(LocalDate month, long revenueAmount) {
    }

    public record DepartmentRevenueShare(String departmentName, long revenueAmount, BigDecimal percentageShare) {
    }
}
