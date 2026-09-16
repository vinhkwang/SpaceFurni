package com.spacefurni.catalog.application;

import com.spacefurni.catalog.infrastructure.ProductRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductRatingAggregateService {

    private final ProductRepository productRepository;

    public ProductRatingAggregateService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void recomputeRatingAggregate(UUID productId, BigDecimal ratingAverage, int reviewCount) {
        productRepository.updateRatingAggregate(productId, ratingAverage, reviewCount);
    }
}
