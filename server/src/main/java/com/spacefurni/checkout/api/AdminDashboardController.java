package com.spacefurni.checkout.api;

import com.spacefurni.checkout.api.dto.DepartmentRevenueShareResponse;
import com.spacefurni.checkout.api.dto.MonthlyRevenuePointResponse;
import com.spacefurni.checkout.application.AdminDashboardAnalyticsQueryService;
import com.spacefurni.shared.api.ApiResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private static final int DEFAULT_REVENUE_MONTHS = 12;
    private static final int MAX_REVENUE_MONTHS = 24;

    private final AdminDashboardAnalyticsQueryService adminDashboardAnalyticsQueryService;

    public AdminDashboardController(AdminDashboardAnalyticsQueryService adminDashboardAnalyticsQueryService) {
        this.adminDashboardAnalyticsQueryService = adminDashboardAnalyticsQueryService;
    }

    @GetMapping("/revenue")
    public ApiResponse<List<MonthlyRevenuePointResponse>> getMonthlyRevenue(
            @RequestParam(required = false) Integer months) {
        List<AdminDashboardAnalyticsQueryService.MonthlyRevenuePoint> points = adminDashboardAnalyticsQueryService
                .monthlyRevenue(resolveMonths(months));
        return ApiResponse.success(points.stream()
                .map(point -> new MonthlyRevenuePointResponse(point.month(), point.revenueAmount())).toList());
    }

    @GetMapping("/department-share")
    public ApiResponse<List<DepartmentRevenueShareResponse>> getDepartmentRevenueShare() {
        List<AdminDashboardAnalyticsQueryService.DepartmentRevenueShare> shares = adminDashboardAnalyticsQueryService
                .revenueShareByDepartment();
        return ApiResponse.success(shares.stream().map(share -> new DepartmentRevenueShareResponse(
                share.departmentName(), share.revenueAmount(), share.percentageShare())).toList());
    }

    private int resolveMonths(Integer requestedMonths) {
        if (requestedMonths == null) {
            return DEFAULT_REVENUE_MONTHS;
        }
        return Math.max(1, Math.min(requestedMonths, MAX_REVENUE_MONTHS));
    }
}
