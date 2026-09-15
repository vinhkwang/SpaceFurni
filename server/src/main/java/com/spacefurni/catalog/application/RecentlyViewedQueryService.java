package com.spacefurni.catalog.application;

import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.api.mapper.ProductResponseMapper;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.RecentlyViewedProduct;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.catalog.infrastructure.RecentlyViewedProductRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecentlyViewedQueryService {

    private static final int MAX_RESULTS = 12;

    private final RecentlyViewedProductRepository recentlyViewedProductRepository;
    private final ProductRepository productRepository;
    private final ProductResponseMapper productResponseMapper;

    public RecentlyViewedQueryService(RecentlyViewedProductRepository recentlyViewedProductRepository,
            ProductRepository productRepository, ProductResponseMapper productResponseMapper) {
        this.recentlyViewedProductRepository = recentlyViewedProductRepository;
        this.productRepository = productRepository;
        this.productResponseMapper = productResponseMapper;
    }

    public List<ProductSummaryResponse> recentlyViewedFor(UUID userId, UUID guestToken, UUID excludedProductId) {
        List<RecentlyViewedProduct> views = findViews(userId, guestToken, excludedProductId);
        List<UUID> orderedProductIds = views.stream().map(RecentlyViewedProduct::getProductId).toList();
        Map<UUID, Product> productsById = productRepository.findAllByIdIn(orderedProductIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        return orderedProductIds.stream().map(productsById::get).filter(Objects::nonNull)
                .map(productResponseMapper::toSummary).toList();
    }

    private List<RecentlyViewedProduct> findViews(UUID userId, UUID guestToken, UUID excludedProductId) {
        PageRequest pageRequest = PageRequest.of(0, MAX_RESULTS);
        if (excludedProductId == null) {
            return userId != null ? recentlyViewedProductRepository.findByUserIdOrderByViewedAtDesc(userId,
                    pageRequest) : recentlyViewedProductRepository.findByGuestTokenOrderByViewedAtDesc(guestToken,
                    pageRequest);
        }
        return userId != null
                ? recentlyViewedProductRepository.findByUserIdAndProductIdNotOrderByViewedAtDesc(userId,
                        excludedProductId, pageRequest)
                : recentlyViewedProductRepository.findByGuestTokenAndProductIdNotOrderByViewedAtDesc(guestToken,
                        excludedProductId, pageRequest);
    }
}
