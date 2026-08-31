package com.spacefurni.checkout.infrastructure;

import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSearchSpecifications {

    private OrderSearchSpecifications() {
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Order> orderNumberOrCustomerNameContains(String term) {
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + term.toLowerCase() + "%";
            return criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNumber")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("deliveryDetails").get("fullName")),
                            pattern));
        };
    }
}
