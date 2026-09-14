package com.spacefurni.shared.notification;

import com.spacefurni.checkout.api.dto.OrderResponse;
import com.spacefurni.checkout.application.OrderQueryService;
import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.domain.OrderPlacedEvent;
import com.spacefurni.shared.domain.OrderStatusChangedEvent;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderNotificationListener {

    private final NotificationService notificationService;
    private final OrderQueryService orderQueryService;
    private final CurrentUserQueryService currentUserQueryService;

    public OrderNotificationListener(NotificationService notificationService, OrderQueryService orderQueryService,
            CurrentUserQueryService currentUserQueryService) {
        this.notificationService = notificationService;
        this.orderQueryService = orderQueryService;
        this.currentUserQueryService = currentUserQueryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        String recipientEmail = currentUserQueryService.getEmailById(event.userId());
        OrderResponse order = orderQueryService.findOrderDetail(event.userId(), event.orderNumber());
        notificationService.sendOrderConfirmation(recipientEmail, event.orderNumber(), toOrderLines(order),
                new Money(order.totalAmount(), order.currencyCode()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        String recipientEmail = currentUserQueryService.getEmailById(event.userId());
        notificationService.sendOrderStatusUpdate(recipientEmail, event.orderNumber(), event.previousStatus(),
                event.newStatus());
    }

    private List<NotificationService.OrderLine> toOrderLines(OrderResponse order) {
        return order.items().stream()
                .map(item -> new NotificationService.OrderLine(item.productName(), item.quantity(),
                        new Money(item.lineTotalAmount(), order.currencyCode())))
                .toList();
    }
}
