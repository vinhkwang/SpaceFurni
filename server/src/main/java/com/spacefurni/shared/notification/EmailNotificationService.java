package com.spacefurni.shared.notification;

import com.spacefurni.shared.domain.Money;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final NotificationProperties notificationProperties;

    public EmailNotificationService(JavaMailSender javaMailSender, TemplateEngine templateEngine,
            NotificationProperties notificationProperties) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.notificationProperties = notificationProperties;
    }

    @Override
    public void sendOrderConfirmation(String recipientEmail, String orderNumber, List<OrderLine> lines,
            Money total) {
        Context context = new Context();
        context.setVariable("orderNumber", orderNumber);
        context.setVariable("lines", lines.stream().map(this::toLineView).toList());
        context.setVariable("total", formatMoney(total));
        sendTemplatedEmail(recipientEmail, "Your SpaceFurni order " + orderNumber + " is confirmed",
                "order-confirmed", context);
    }

    @Override
    public void sendOrderStatusUpdate(String recipientEmail, String orderNumber, String previousStatus,
            String newStatus) {
        Context context = new Context();
        context.setVariable("orderNumber", orderNumber);
        context.setVariable("previousStatus", previousStatus);
        context.setVariable("newStatus", newStatus);
        sendTemplatedEmail(recipientEmail, "Your SpaceFurni order " + orderNumber + " is now " + newStatus,
                "order-status-changed", context);
    }

    @Override
    public void sendLowStockAlert(String productName, int quantityOnHand, int threshold) {
        Context context = new Context();
        context.setVariable("productName", productName);
        context.setVariable("quantityOnHand", quantityOnHand);
        context.setVariable("threshold", threshold);
        sendTemplatedEmail(notificationProperties.lowStockAlertRecipient(), "Low stock: " + productName,
                "low-stock-alert", context);
    }

    private void sendTemplatedEmail(String recipientEmail, String subject, String templateName, Context context) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(templateName, context), true);
        } catch (MessagingException exception) {
            throw new MailParseException(exception);
        }
        javaMailSender.send(mimeMessage);
    }

    private OrderLineView toLineView(OrderLine line) {
        return new OrderLineView(line.productName(), line.quantity(), formatMoney(line.lineTotal()));
    }

    private String formatMoney(Money money) {
        return "%,d %s".formatted(money.amount(), money.currencyCode());
    }

    private record OrderLineView(String productName, int quantity, String lineTotal) {
    }
}
