package com.spacefurni.pricing.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.pricing.domain.Promotion;
import com.spacefurni.pricing.domain.PromotionType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PromotionRepositoryTest {

    @Autowired
    private PromotionRepository promotionRepository;

    @Test
    void persistsAndReloadsPromotionByCode() {
        String code = "TEST" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Promotion promotion = new Promotion(code, PromotionType.FIXED_AMOUNT, 200_000L, 1_000_000L, true, null, null);

        promotionRepository.saveAndFlush(promotion);

        Promotion reloaded = promotionRepository.findById(code).orElseThrow();
        assertThat(reloaded.getType()).isEqualTo(PromotionType.FIXED_AMOUNT);
        assertThat(reloaded.getValue()).isEqualTo(200_000L);
        assertThat(reloaded.getMinimumSubtotalAmount()).isEqualTo(1_000_000L);
        assertThat(reloaded.getIsActive()).isTrue();
    }
}
