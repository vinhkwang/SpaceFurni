package com.spacefurni.checkout.infrastructure;

import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "items")
    Page<Order> findAllByUserIdOrderByPlacedAtDesc(UUID userId, Pageable pageable);

    @Query("select o.status as status, count(o) as total from Order o group by o.status")
    List<OrderStatusCount> countGroupedByStatus();

    interface OrderStatusCount {
        OrderStatus getStatus();

        long getTotal();
    }
}
