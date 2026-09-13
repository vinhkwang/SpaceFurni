package com.spacefurni.shared.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spacefurni.notification")
public record NotificationProperties(String lowStockAlertRecipient) {
}
