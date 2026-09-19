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

    @Query(value = CUSTOMER_AGGREGATE_QUERY, countQuery = CUSTOMER_AGGREGATE_COUNT_QUERY, nativeQuery = true)
    Page<CustomerAggregateRow> findCustomerAggregates(@Param("searchPattern") String searchPattern,
            @Param("minOrderCount") Integer minOrderCount, @Param("maxOrderCount") Integer maxOrderCount,
            Pageable pageable);

    @Query(value = CUSTOMER_AGGREGATE_QUERY, nativeQuery = true)
    List<CustomerAggregateRow> findAllCustomerAggregates(@Param("searchPattern") String searchPattern,
            @Param("minOrderCount") Integer minOrderCount, @Param("maxOrderCount") Integer maxOrderCount);

    @Query(value = """
            SELECT u.id AS id, u.full_name AS fullName, u.email AS email, latest.delivery_district AS district,
                   COUNT(o.id) AS orderCount, COALESCE(SUM(o.total_amount), 0)::bigint AS lifetimeValueAmount,
                   COALESCE(MAX(o.currency_code), 'VND') AS currencyCode, MIN(o.placed_at) AS firstOrderAt
              FROM users u
              JOIN orders o ON o.user_id = u.id AND o.status <> 'CANCELLED'
              JOIN LATERAL (
                  SELECT o2.delivery_district
                    FROM orders o2
                   WHERE o2.user_id = u.id AND o2.status <> 'CANCELLED'
                   ORDER BY o2.placed_at DESC
                   LIMIT 1
              ) latest ON true
             WHERE u.id = :customerId
             GROUP BY u.id, u.full_name, u.email, latest.delivery_district
            """, nativeQuery = true)
    Optional<CustomerAggregateRow> findCustomerAggregateById(@Param("customerId") UUID customerId);

    @Query(value = """
            SELECT CASE WHEN order_count >= 3 THEN 'VIP' WHEN order_count = 2 THEN 'RETURNING' ELSE 'NEW' END AS tier,
                   COUNT(*) AS total
              FROM (
                  SELECT o.user_id, COUNT(*) AS order_count
                    FROM orders o
                   WHERE o.status <> 'CANCELLED'
                   GROUP BY o.user_id
              ) per_customer
             GROUP BY tier
            """, nativeQuery = true)
    List<CustomerTierCount> countGroupedByTier();

    String CUSTOMER_AGGREGATE_QUERY = """
            SELECT u.id AS id, u.full_name AS fullName, u.email AS email, latest.delivery_district AS district,
                   COUNT(o.id) AS orderCount, COALESCE(SUM(o.total_amount), 0)::bigint AS lifetimeValueAmount,
                   COALESCE(MAX(o.currency_code), 'VND') AS currencyCode, MIN(o.placed_at) AS firstOrderAt
              FROM users u
              JOIN orders o ON o.user_id = u.id AND o.status <> 'CANCELLED'
              JOIN LATERAL (
                  SELECT o2.delivery_district
                    FROM orders o2
                   WHERE o2.user_id = u.id AND o2.status <> 'CANCELLED'
                   ORDER BY o2.placed_at DESC
                   LIMIT 1
              ) latest ON true
             WHERE (:searchPattern IS NULL OR LOWER(u.full_name) LIKE :searchPattern
                    OR LOWER(u.email) LIKE :searchPattern)
             GROUP BY u.id, u.full_name, u.email, latest.delivery_district
            HAVING (:minOrderCount IS NULL OR COUNT(o.id) >= :minOrderCount)
               AND (:maxOrderCount IS NULL OR COUNT(o.id) <= :maxOrderCount)
             ORDER BY lifetimeValueAmount DESC
            """;

    String CUSTOMER_AGGREGATE_COUNT_QUERY = """
            SELECT COUNT(*) FROM (
                SELECT u.id
                  FROM users u
                  JOIN orders o ON o.user_id = u.id AND o.status <> 'CANCELLED'
                 WHERE (:searchPattern IS NULL OR LOWER(u.full_name) LIKE :searchPattern
                        OR LOWER(u.email) LIKE :searchPattern)
                 GROUP BY u.id
                HAVING (:minOrderCount IS NULL OR COUNT(o.id) >= :minOrderCount)
                   AND (:maxOrderCount IS NULL OR COUNT(o.id) <= :maxOrderCount)
            ) matched_customers
            """;

    interface CustomerAggregateRow {
        UUID getId();

        String getFullName();

        String getEmail();

        String getDistrict();

        long getOrderCount();

        long getLifetimeValueAmount();

        String getCurrencyCode();

        Instant getFirstOrderAt();
    }

    interface CustomerTierCount {
        String getTier();

        long getTotal();
    }

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
