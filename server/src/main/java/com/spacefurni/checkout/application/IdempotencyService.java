package com.spacefurni.checkout.application;

import com.spacefurni.checkout.domain.IdempotencyKey;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.infrastructure.IdempotencyKeyRepository;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final OrderRepository orderRepository;

    public IdempotencyService(IdempotencyKeyRepository idempotencyKeyRepository, OrderRepository orderRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Optional<Order> registerOrReturnExisting(String key, UUID userId, String requestFingerprint) {
        int insertedRowCount = idempotencyKeyRepository.insertIfAbsent(key, userId, requestFingerprint);
        if (insertedRowCount == 1) {
            return Optional.empty();
        }
        return Optional.of(resolveReplay(key, requestFingerprint));
    }

    @Transactional
    public void assignOrder(String key, UUID orderId) {
        idempotencyKeyRepository.findById(key).orElseThrow().assignOrder(orderId);
    }

    private Order resolveReplay(String key, String requestFingerprint) {
        IdempotencyKey existing = idempotencyKeyRepository.findById(key).orElseThrow();
        if (!existing.getRequestFingerprint().equals(requestFingerprint)) {
            throw new IdempotencyKeyConflictException(key);
        }
        return orderRepository.findById(existing.getOrderId()).orElseThrow();
    }
}
