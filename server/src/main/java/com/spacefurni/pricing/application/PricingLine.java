package com.spacefurni.pricing.application;

import com.spacefurni.shared.domain.Money;

public record PricingLine(Money unitPrice, int quantity) {
}
