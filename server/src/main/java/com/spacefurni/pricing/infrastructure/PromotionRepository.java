package com.spacefurni.pricing.infrastructure;

import com.spacefurni.pricing.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, String> {
}
