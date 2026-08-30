package com.spacefurni.checkout.application;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderNumberGenerator {

    private final EntityManager entityManager;

    public OrderNumberGenerator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public String generate() {
        Number nextValue = (Number) entityManager.createNativeQuery("SELECT nextval('order_number_seq')")
                .getSingleResult();
        return "SF-%d".formatted(nextValue.longValue());
    }
}
