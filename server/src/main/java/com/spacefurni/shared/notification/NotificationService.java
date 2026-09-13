package com.spacefurni.shared.notification;

import com.spacefurni.shared.domain.Money;
import java.util.List;

public interface NotificationService {

    void sendOrderConfirmation(String recipientEmail, String orderNumber, List<OrderLine> lines, Money total);

    void sendOrderStatusUpdate(String recipientEmail, String orderNumber, String previousStatus, String newStatus);

    void sendLowStockAlert(String productName, int quantityOnHand, int threshold);

    record OrderLine(String productName, int quantity, Money lineTotal) {
    }
}
