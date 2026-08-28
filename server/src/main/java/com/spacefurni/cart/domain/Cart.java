package com.spacefurni.cart.domain;

import com.spacefurni.shared.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class Cart extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "guest_token")
    private UUID guestToken;

    @Column(name = "promotion_code")
    private String promotionCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new LinkedHashSet<>();

    protected Cart() {
    }

    public Cart(UUID userId, UUID guestToken) {
        this.userId = userId;
        this.guestToken = guestToken;
        this.status = CartStatus.ACTIVE;
    }

    public CartItem addOrIncrementLine(UUID productId, int quantity) {
        Optional<CartItem> existingLine = findLineByProductId(productId);
        if (existingLine.isPresent()) {
            existingLine.get().incrementQuantity(quantity);
            return existingLine.get();
        }
        CartItem newLine = new CartItem(this, productId, quantity);
        items.add(newLine);
        return newLine;
    }

    public Optional<CartItem> findLineByProductId(UUID productId) {
        return items.stream().filter(item -> item.getProductId().equals(productId)).findFirst();
    }

    public void setLineQuantity(UUID productId, int quantity) {
        findLineByProductId(productId).ifPresent(line -> line.setQuantity(quantity));
    }

    public void removeLine(UUID productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void clearLines() {
        items.clear();
    }

    public void markConverted() {
        this.status = CartStatus.CONVERTED;
    }

    public void applyPromotion(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public void clearPromotion() {
        this.promotionCode = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getGuestToken() {
        return guestToken;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public CartStatus getStatus() {
        return status;
    }

    public Set<CartItem> getItems() {
        return items;
    }
}
