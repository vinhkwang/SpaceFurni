package com.spacefurni.shared.notification;

import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.application.CatalogQueryService;
import com.spacefurni.shared.domain.LowStockEvent;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InventoryNotificationListener {

    private final NotificationService notificationService;
    private final CatalogQueryService catalogQueryService;

    public InventoryNotificationListener(NotificationService notificationService,
            CatalogQueryService catalogQueryService) {
        this.notificationService = notificationService;
        this.catalogQueryService = catalogQueryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStock(LowStockEvent event) {
        Map<UUID, ProductSummaryResponse> productsById = catalogQueryService
                .findProductSummariesByIds(List.of(event.productId()));
        String productName = productsById.get(event.productId()).name();
        notificationService.sendLowStockAlert(productName, event.quantityOnHand(), event.threshold());
    }
}
