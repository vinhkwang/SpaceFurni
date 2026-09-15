package com.spacefurni.catalog.application;

import com.spacefurni.catalog.infrastructure.RecentlyViewedProductRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecentlyViewedService {

    private static final int RETAINED_VIEW_COUNT = 12;

    private final RecentlyViewedProductRepository recentlyViewedProductRepository;

    public RecentlyViewedService(RecentlyViewedProductRepository recentlyViewedProductRepository) {
        this.recentlyViewedProductRepository = recentlyViewedProductRepository;
    }

    @Transactional
    public void recordView(UUID userId, UUID guestToken, UUID productId) {
        if (userId != null) {
            recentlyViewedProductRepository.upsertViewForUser(userId, productId);
            recentlyViewedProductRepository.evictBeyondRetainedCountForUser(userId, RETAINED_VIEW_COUNT);
        } else {
            recentlyViewedProductRepository.upsertViewForGuest(guestToken, productId);
            recentlyViewedProductRepository.evictBeyondRetainedCountForGuest(guestToken, RETAINED_VIEW_COUNT);
        }
    }
}
