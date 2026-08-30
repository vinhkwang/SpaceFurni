package com.spacefurni.checkout.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.IdempotencyKey;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import jakarta.persistence.PersistenceException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class IdempotencyKeyRepositoryTest {

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID persistUser() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        return entityManager.persistAndFlush(user).getId();
    }

    private Order persistOrder(UUID userId) {
        DeliveryDetails deliveryDetails = new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1",
                "Ho Chi Minh City", null);
        Order order = new Order("SF-" + System.nanoTime(), userId, Money.ofVnd(1_000_000L), Money.ofVnd(300_000L),
                Money.zeroVnd(), Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CARD);
        return entityManager.persistAndFlush(order);
    }

    @Test
    void persistsAndReloadsKeyWithNoOrderYetAssigned() {
        UUID userId = persistUser();
        String key = UUID.randomUUID().toString();

        idempotencyKeyRepository.saveAndFlush(new IdempotencyKey(key, userId, "fingerprint-1"));

        IdempotencyKey reloaded = idempotencyKeyRepository.findById(key).orElseThrow();
        assertThat(reloaded.getUserId()).isEqualTo(userId);
        assertThat(reloaded.getRequestFingerprint()).isEqualTo("fingerprint-1");
        assertThat(reloaded.getOrderId()).isNull();
    }

    @Test
    void assignOrderPersistsTheOrderIdOnceKnown() {
        UUID userId = persistUser();
        Order order = persistOrder(userId);
        String key = UUID.randomUUID().toString();
        IdempotencyKey idempotencyKey = idempotencyKeyRepository
                .saveAndFlush(new IdempotencyKey(key, userId, "fingerprint-1"));

        idempotencyKey.assignOrder(order.getId());
        idempotencyKeyRepository.saveAndFlush(idempotencyKey);
        entityManager.clear();

        assertThat(idempotencyKeyRepository.findById(key).orElseThrow().getOrderId()).isEqualTo(order.getId());
    }

    @Test
    void duplicateKeyPersistViolatesThePrimaryKeyConstraint() {
        UUID userId = persistUser();
        String key = UUID.randomUUID().toString();
        entityManager.persistAndFlush(new IdempotencyKey(key, userId, "fingerprint-1"));

        assertThatThrownBy(
                () -> entityManager.persistAndFlush(new IdempotencyKey(key, userId, "fingerprint-2")))
                .isInstanceOf(PersistenceException.class);
    }
}
