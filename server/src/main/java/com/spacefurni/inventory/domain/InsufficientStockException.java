package com.spacefurni.inventory.domain;

import com.spacefurni.shared.exception.ConflictException;
import com.spacefurni.shared.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class InsufficientStockException extends ConflictException {

    private final UUID productId;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(UUID productId, int requestedQuantity, int availableQuantity) {
        super(ErrorCode.INSUFFICIENT_STOCK,
                "Insufficient stock for product " + productId + ": requested " + requestedQuantity
                        + " but only " + availableQuantity + " available",
                Map.of("productId", productId.toString(), "requestedQuantity", String.valueOf(requestedQuantity),
                        "availableQuantity", String.valueOf(availableQuantity)));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
