package com.spacefurni.checkout.infrastructure;

import com.spacefurni.checkout.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
}
