package com.spacefurni.checkout.application;

import com.spacefurni.cart.application.CartService;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartItem;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.application.CatalogQueryService;
import com.spacefurni.checkout.api.dto.DeliveryDetailsRequest;
import com.spacefurni.checkout.api.dto.PlaceOrderRequest;
import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.Payment;
import com.spacefurni.checkout.domain.PaymentResult;
import com.spacefurni.checkout.domain.PaymentStatus;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.checkout.infrastructure.PaymentRepository;
import com.spacefurni.inventory.api.dto.StockReservationLine;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.pricing.application.PricingLine;
import com.spacefurni.pricing.application.PricingService;
import com.spacefurni.pricing.domain.PriceBreakdown;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.BusinessRuleViolationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private final IdempotencyService idempotencyService;
    private final CartService cartService;
    private final CatalogQueryService catalogQueryService;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderRepository orderRepository;
    private final PaymentStrategyRegistry paymentStrategyRegistry;
    private final PaymentRepository paymentRepository;

    public CheckoutService(IdempotencyService idempotencyService, CartService cartService,
            CatalogQueryService catalogQueryService, InventoryService inventoryService,
            PricingService pricingService, OrderNumberGenerator orderNumberGenerator,
            OrderRepository orderRepository, PaymentStrategyRegistry paymentStrategyRegistry,
            PaymentRepository paymentRepository) {
        this.idempotencyService = idempotencyService;
        this.cartService = cartService;
        this.catalogQueryService = catalogQueryService;
        this.inventoryService = inventoryService;
        this.pricingService = pricingService;
        this.orderNumberGenerator = orderNumberGenerator;
        this.orderRepository = orderRepository;
        this.paymentStrategyRegistry = paymentStrategyRegistry;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Order placeOrder(UUID userId, String idempotencyKey, PlaceOrderRequest request) {
        Optional<Order> replayedOrder = registerOrReturnExistingOrder(idempotencyKey, userId, request);
        if (replayedOrder.isPresent()) {
            return replayedOrder.get();
        }
        Cart cart = loadNonEmptyActiveCart(userId);
        Map<UUID, ProductSummaryResponse> productsByProductId = fetchProductSnapshots(cart);
        reserveStockForCartLines(cart);
        PriceBreakdown priceBreakdown = calculatePricing(cart, productsByProductId, request);
        Order order = buildPersistAndLinkOrderToIdempotencyKey(idempotencyKey, userId, cart, productsByProductId,
                priceBreakdown, request);
        PaymentResult paymentResult = executePayment(order);
        persistPaymentRejectingFailure(order, paymentResult);
        transitionOrderForPaymentResult(order, paymentResult);
        markCartConverted(cart);
        return order;
    }

    private Optional<Order> registerOrReturnExistingOrder(String idempotencyKey, UUID userId,
            PlaceOrderRequest request) {
        return idempotencyService.registerOrReturnExisting(idempotencyKey, userId, request.toString());
    }

    private Cart loadNonEmptyActiveCart(UUID userId) {
        Cart cart = cartService.resolveOrCreateActiveCart(userId, null);
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleViolationException("Cannot place an order from an empty cart");
        }
        return cart;
    }

    private Map<UUID, ProductSummaryResponse> fetchProductSnapshots(Cart cart) {
        List<UUID> productIds = cart.getItems().stream().map(CartItem::getProductId).toList();
        return catalogQueryService.findProductSummariesByIds(productIds);
    }

    private void reserveStockForCartLines(Cart cart) {
        List<StockReservationLine> lines = cart.getItems().stream()
                .map(item -> new StockReservationLine(item.getProductId(), item.getQuantity())).toList();
        inventoryService.reserveStockForOrderLines(lines);
    }

    private PriceBreakdown calculatePricing(Cart cart, Map<UUID, ProductSummaryResponse> productsByProductId,
            PlaceOrderRequest request) {
        List<PricingLine> pricingLines = cart.getItems().stream()
                .map(item -> toPricingLine(item, productsByProductId.get(item.getProductId()))).toList();
        return pricingService.calculate(pricingLines, cart.getPromotionCode(), request.deliveryWindow());
    }

    private PricingLine toPricingLine(CartItem item, ProductSummaryResponse product) {
        return new PricingLine(new Money(product.priceAmount(), product.currencyCode()), item.getQuantity());
    }

    private Order buildPersistAndLinkOrderToIdempotencyKey(String idempotencyKey, UUID userId, Cart cart,
            Map<UUID, ProductSummaryResponse> productsByProductId, PriceBreakdown priceBreakdown,
            PlaceOrderRequest request) {
        Order order = new Order(orderNumberGenerator.generate(), userId, priceBreakdown.subtotal(),
                priceBreakdown.shipping(), priceBreakdown.discount(), priceBreakdown.total(),
                priceBreakdown.appliedPromotionCode(), toDeliveryDetails(request.deliveryDetails()),
                request.deliveryWindow(), request.paymentMethod());
        for (CartItem item : cart.getItems()) {
            order.addItem(toOrderItem(item, productsByProductId.get(item.getProductId())));
        }
        order = orderRepository.save(order);
        idempotencyService.assignOrder(idempotencyKey, order.getId());
        return order;
    }

    private DeliveryDetails toDeliveryDetails(DeliveryDetailsRequest request) {
        return new DeliveryDetails(request.fullName(), request.phone(), request.street(), request.district(),
                request.city(), request.note());
    }

    private OrderItem toOrderItem(CartItem item, ProductSummaryResponse product) {
        Money unitPrice = new Money(product.priceAmount(), product.currencyCode());
        Money lineTotal = unitPrice.multipliedBy(item.getQuantity());
        return new OrderItem(product.id(), product.name(), product.sku(), unitPrice.amount(), item.getQuantity(),
                lineTotal.amount());
    }

    private PaymentResult executePayment(Order order) {
        return paymentStrategyRegistry.resolve(order.getPaymentMethod()).execute(order);
    }

    private void persistPaymentRejectingFailure(Order order, PaymentResult paymentResult) {
        paymentRepository.save(new Payment(order, order.getPaymentMethod(), paymentResult.status(),
                paymentResult.providerReference(), order.getTotal().amount(), paymentResult.failureReason()));
        if (paymentResult.status() == PaymentStatus.FAILED) {
            throw new PaymentFailedException(paymentResult.failureReason());
        }
    }

    private void transitionOrderForPaymentResult(Order order, PaymentResult paymentResult) {
        order.recordPaymentStatus(paymentResult.status());
        if (paymentResult.status() == PaymentStatus.CAPTURED) {
            order.transitionTo(OrderStatus.PAID);
        }
    }

    private void markCartConverted(Cart cart) {
        cartService.markConverted(cart);
    }
}
