package com.spacefurni.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class IdempotencyServiceTest extends AbstractIntegrationTest {

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private UUID persistUser() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        return userRepository.saveAndFlush(user).getId();
    }

    private Order persistOrder(UUID userId) {
        DeliveryDetails deliveryDetails = new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1",
                "Ho Chi Minh City", null);
        Order order = new Order("SF-" + System.nanoTime(), userId, Money.ofVnd(1_000_000L), Money.ofVnd(300_000L),
                Money.zeroVnd(), Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CARD);
        return orderRepository.saveAndFlush(order);
    }

    @Test
    void registerOrReturnExistingReturnsEmptyForAFreshKey() {
        UUID userId = persistUser();
        String key = UUID.randomUUID().toString();

        Optional<Order> result = idempotencyService.registerOrReturnExisting(key, userId, "fingerprint-1");

        assertThat(result).isEmpty();
    }

    @Test
    void registerOrReturnExistingReturnsThePreviouslyCreatedOrderOnReplay() {
        UUID userId = persistUser();
        String key = UUID.randomUUID().toString();
        idempotencyService.registerOrReturnExisting(key, userId, "fingerprint-1");
        Order order = persistOrder(userId);
        idempotencyService.assignOrder(key, order.getId());

        Optional<Order> replay = idempotencyService.registerOrReturnExisting(key, userId, "fingerprint-1");

        assertThat(replay).isPresent();
        assertThat(replay.get().getId()).isEqualTo(order.getId());
    }

    @Test
    void registerOrReturnExistingRejectsTheSameKeyWithADifferentFingerprint() {
        UUID userId = persistUser();
        String key = UUID.randomUUID().toString();
        idempotencyService.registerOrReturnExisting(key, userId, "fingerprint-1");
        Order order = persistOrder(userId);
        idempotencyService.assignOrder(key, order.getId());

        assertThatThrownBy(() -> idempotencyService.registerOrReturnExisting(key, userId, "fingerprint-2"))
                .isInstanceOf(IdempotencyKeyConflictException.class);
    }
}
