package com.spacefurni.checkout.domain;

import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount", nullable = false)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "currency_code", nullable = false))
    })
    private Money subtotal;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "shipping_amount", nullable = false)),
            @AttributeOverride(name = "currencyCode",
                    column = @Column(name = "currency_code", insertable = false, updatable = false))
    })
    private Money shipping;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "discount_amount", nullable = false)),
            @AttributeOverride(name = "currencyCode",
                    column = @Column(name = "currency_code", insertable = false, updatable = false))
    })
    private Money discount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false)),
            @AttributeOverride(name = "currencyCode",
                    column = @Column(name = "currency_code", insertable = false, updatable = false))
    })
    private Money total;

    @Column(name = "promotion_code")
    private String promotionCode;

    @Embedded
    private DeliveryDetails deliveryDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_window", nullable = false)
    private DeliveryWindow deliveryWindow;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new LinkedHashSet<>();

    protected Order() {
    }

    public Order(String orderNumber, UUID userId, Money subtotal, Money shipping, Money discount, Money total,
            String promotionCode, DeliveryDetails deliveryDetails, DeliveryWindow deliveryWindow,
            PaymentMethod paymentMethod) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.status = OrderStatus.PENDING;
        this.subtotal = subtotal;
        this.shipping = shipping;
        this.discount = discount;
        this.total = total;
        this.promotionCode = promotionCode;
        this.deliveryDetails = deliveryDetails;
        this.deliveryWindow = deliveryWindow;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.PENDING;
        this.placedAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.assignToOrder(this);
    }

    public void transitionTo(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new BusinessRuleViolationException("Cannot transition order from " + status + " to " + target);
        }
        this.status = target;
    }

    public UUID getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public UUID getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public Money getShipping() {
        return shipping;
    }

    public Money getDiscount() {
        return discount;
    }

    public Money getTotal() {
        return total;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public DeliveryDetails getDeliveryDetails() {
        return deliveryDetails;
    }

    public DeliveryWindow getDeliveryWindow() {
        return deliveryWindow;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public Long getVersion() {
        return version;
    }

    public Set<OrderItem> getItems() {
        return items;
    }
}
