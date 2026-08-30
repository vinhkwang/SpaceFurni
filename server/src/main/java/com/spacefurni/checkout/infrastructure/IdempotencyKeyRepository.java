package com.spacefurni.checkout.infrastructure;

import com.spacefurni.checkout.domain.IdempotencyKey;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

    @Modifying
    @Query(value = "insert into idempotency_keys (key, user_id, request_fingerprint, created_at) "
            + "values (:key, :userId, :requestFingerprint, now()) on conflict (key) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("key") String key, @Param("userId") UUID userId,
            @Param("requestFingerprint") String requestFingerprint);
}
