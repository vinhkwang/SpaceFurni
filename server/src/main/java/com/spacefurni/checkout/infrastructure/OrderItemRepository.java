package com.spacefurni.checkout.infrastructure;

import com.spacefurni.checkout.domain.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findAllByOrder_IdIn(List<UUID> orderIds);
}
