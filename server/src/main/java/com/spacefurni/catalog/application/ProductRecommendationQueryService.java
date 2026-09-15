package com.spacefurni.catalog.application;

import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.api.mapper.ProductResponseMapper;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductRecommendationQueryService {

    private final ProductRepository productRepository;
    private final ProductResponseMapper productResponseMapper;

    public ProductRecommendationQueryService(ProductRepository productRepository,
            ProductResponseMapper productResponseMapper) {
        this.productRepository = productRepository;
        this.productResponseMapper = productResponseMapper;
    }

    public List<ProductSummaryResponse> recommendationsFor(UUID productId, int limit) {
        List<ProductRepository.CoPurchaseRow> coPurchaseRows = productRepository.findCoPurchasedProducts(productId,
                limit);
        List<UUID> orderedProductIds = coPurchaseRows.stream().map(ProductRepository.CoPurchaseRow::getProductId)
                .toList();
        Map<UUID, Product> productsById = productRepository.findAllByIdIn(orderedProductIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        return orderedProductIds.stream().map(productsById::get).filter(Objects::nonNull)
                .map(productResponseMapper::toSummary).toList();
    }
}
