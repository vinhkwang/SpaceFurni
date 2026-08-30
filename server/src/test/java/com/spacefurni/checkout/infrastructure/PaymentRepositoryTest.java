package com.spacefurni.checkout.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.Payment;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.PaymentStatus;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
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
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Order persistOrder() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        UUID userId = entityManager.persistAndFlush(user).getId();
        DeliveryDetails deliveryDetails = new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1",
                "Ho Chi Minh City", null);
        Order order = new Order("SF-2419", userId, Money.ofVnd(1_000_000L), Money.ofVnd(300_000L), Money.zeroVnd(),
                Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD, PaymentMethod.CARD);
        return entityManager.persistAndFlush(order);
    }

    @Test
    void persistsAndReloadsPaymentLinkedToItsOrder() {
        Order order = persistOrder();
        Payment payment = new Payment(order, PaymentMethod.CARD, PaymentStatus.CAPTURED, "PAY-REF-1", 1_300_000L,
                null);

        UUID id = paymentRepository.saveAndFlush(payment).getId();
        entityManager.clear();

        Payment reloaded = paymentRepository.findById(id).orElseThrow();
        assertThat(reloaded.getOrder().getId()).isEqualTo(order.getId());
        assertThat(reloaded.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(reloaded.getProviderReference()).isEqualTo("PAY-REF-1");
        assertThat(reloaded.getAmount()).isEqualTo(1_300_000L);
        assertThat(reloaded.getFailureReason()).isNull();
        assertThat(reloaded.getCreatedAt()).isNotNull();
    }

    @Test
    void persistsFailedPaymentWithFailureReasonAndNoProviderReference() {
        Order order = persistOrder();
        Payment payment = new Payment(order, PaymentMethod.CARD, PaymentStatus.FAILED, null, 1_300_000L,
                "Card declined");

        UUID id = paymentRepository.saveAndFlush(payment).getId();
        entityManager.clear();

        Payment reloaded = paymentRepository.findById(id).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(reloaded.getProviderReference()).isNull();
        assertThat(reloaded.getFailureReason()).isEqualTo("Card declined");
    }
}
