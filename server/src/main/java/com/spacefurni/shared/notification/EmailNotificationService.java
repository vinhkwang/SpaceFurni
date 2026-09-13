package com.spacefurni.shared.notification;

import com.spacefurni.shared.domain.Money;
import java.util.List;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationProperties notificationProperties;

    public EmailNotificationService(JavaMailSender javaMailSender, NotificationProperties notificationProperties) {
        this.javaMailSender = javaMailSender;
        this.notificationProperties = notificationProperties;
    }

    @Override
    public void sendOrderConfirmation(String recipientEmail, String orderNumber, List<OrderLine> lines,
            Money total) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Your SpaceFurni order " + orderNumber + " is confirmed");
        message.setText(orderConfirmationBody(orderNumber, lines, total));
        javaMailSender.send(message);
    }

    @Override
    public void sendOrderStatusUpdate(String recipientEmail, String orderNumber, String previousStatus,
            String newStatus) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Your SpaceFurni order " + orderNumber + " is now " + newStatus);
        message.setText(orderStatusUpdateBody(orderNumber, previousStatus, newStatus));
        javaMailSender.send(message);
    }

    @Override
    public void sendLowStockAlert(String productName, int quantityOnHand, int threshold) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notificationProperties.lowStockAlertRecipient());
        message.setSubject("Low stock: " + productName);
        message.setText(lowStockAlertBody(productName, quantityOnHand, threshold));
        javaMailSender.send(message);
    }

    private String orderConfirmationBody(String orderNumber, List<OrderLine> lines, Money total) {
        StringBuilder body = new StringBuilder("Thank you for your order ").append(orderNumber).append(".\n\n");
        for (OrderLine line : lines) {
            body.append(line.quantity()).append(" x ").append(line.productName()).append(" — ")
                    .append(formatMoney(line.lineTotal())).append('\n');
        }
        body.append("\nTotal: ").append(formatMoney(total));
        return body.toString();
    }

    private String orderStatusUpdateBody(String orderNumber, String previousStatus, String newStatus) {
        return "Order %s changed from %s to %s.".formatted(orderNumber, previousStatus, newStatus);
    }

    private String lowStockAlertBody(String productName, int quantityOnHand, int threshold) {
        return "%s has %d units on hand, below the low-stock threshold of %d."
                .formatted(productName, quantityOnHand, threshold);
    }

    private String formatMoney(Money money) {
        return "%,d %s".formatted(money.amount(), money.currencyCode());
    }
}
