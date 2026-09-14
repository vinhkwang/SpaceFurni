package com.spacefurni.checkout.infrastructure;

import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "items")
    Page<Order> findAllByUserIdOrderByPlacedAtDesc(UUID userId, Pageable pageable);

    @Query("select o.status as status, count(o) as total from Order o group by o.status")
    List<OrderStatusCount> countGroupedByStatus();

    long countByPlacedAtGreaterThanEqual(Instant placedAtInclusiveStart);

    @Query(value = """
            SELECT date_trunc('month', placed_at)::date AS month, SUM(total_amount)::bigint AS revenue
              FROM orders
             WHERE status <> 'CANCELLED'
               AND placed_at >= :sinceInclusive
             GROUP BY month
             ORDER BY month
            """, nativeQuery = true)
    List<MonthlyRevenueRow> findMonthlyRevenueSince(@Param("sinceInclusive") Instant sinceInclusive);

    @Query(value = """
            SELECT department_name AS department, department_revenue AS revenue,
                   ROUND(department_revenue * 100.0 / SUM(department_revenue) OVER (), 2) AS share
              FROM (
                  SELECT COALESCE(parent.name, category.name) AS department_name,
                         SUM(oi.line_total_amount)::bigint AS department_revenue
                    FROM order_items oi
                    JOIN orders o ON o.id = oi.order_id
                    JOIN products p ON p.id = oi.product_id
                    JOIN categories category ON category.id = p.category_id
                    LEFT JOIN categories parent ON parent.id = category.parent_id
                   WHERE o.status <> 'CANCELLED'
                   GROUP BY department_name
              ) department_totals
             ORDER BY revenue DESC
            """, nativeQuery = true)
    List<DepartmentRevenueShareRow> findRevenueShareByDepartment();

    interface OrderStatusCount {
        OrderStatus getStatus();

        long getTotal();
    }

    interface MonthlyRevenueRow {
        LocalDate getMonth();

        long getRevenue();
    }

    interface DepartmentRevenueShareRow {
        String getDepartment();

        long getRevenue();

        BigDecimal getShare();
    }
}
