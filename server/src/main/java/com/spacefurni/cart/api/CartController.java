package com.spacefurni.cart.api;

import com.spacefurni.cart.api.dto.AddCartLineRequest;
import com.spacefurni.cart.api.dto.CartResponse;
import com.spacefurni.cart.api.dto.UpdateCartLineRequest;
import com.spacefurni.cart.api.mapper.CartResponseMapper;
import com.spacefurni.cart.application.CartMergeService;
import com.spacefurni.cart.application.CartService;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartItem;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.application.CatalogQueryService;
import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private static final String GUEST_TOKEN_HEADER = "X-Guest-Token";

    private final CartService cartService;
    private final CartMergeService cartMergeService;
    private final CatalogQueryService catalogQueryService;
    private final CartResponseMapper cartResponseMapper;
    private final CurrentUserQueryService currentUserQueryService;

    public CartController(CartService cartService, CartMergeService cartMergeService,
            CatalogQueryService catalogQueryService, CartResponseMapper cartResponseMapper,
            CurrentUserQueryService currentUserQueryService) {
        this.cartService = cartService;
        this.cartMergeService = cartMergeService;
        this.catalogQueryService = catalogQueryService;
        this.cartResponseMapper = cartResponseMapper;
        this.currentUserQueryService = currentUserQueryService;
    }

    @GetMapping
    public ApiResponse<CartResponse> currentCart(@AuthenticationPrincipal UserDetails principal,
            @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String guestTokenHeader) {
        UUID userId = resolveUserId(principal);
        if (userId == null && guestTokenHeader == null) {
            return ApiResponse.success(new CartResponse(null, null, List.of(), 0L, "VND"));
        }
        UUID guestToken = userId == null ? UUID.fromString(guestTokenHeader) : null;
        Cart cart = cartService.resolveOrCreateActiveCart(userId, guestToken);
        return ApiResponse.success(toResponse(cart));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addLine(@AuthenticationPrincipal UserDetails principal,
            @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String guestTokenHeader,
            @Valid @RequestBody AddCartLineRequest request) {
        Cart cart = resolveCartForWrite(principal, guestTokenHeader);
        cart = cartService.addLine(cart, request.productId(), request.quantity());
        return ApiResponse.success(toResponse(cart));
    }

    @PatchMapping("/items/{productId}")
    public ApiResponse<CartResponse> updateLineQuantity(@AuthenticationPrincipal UserDetails principal,
            @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String guestTokenHeader,
            @PathVariable UUID productId, @Valid @RequestBody UpdateCartLineRequest request) {
        Cart cart = resolveCartForWrite(principal, guestTokenHeader);
        cart = cartService.updateLineQuantity(cart, productId, request.quantity());
        return ApiResponse.success(toResponse(cart));
    }

    @DeleteMapping("/items/{productId}")
    public ApiResponse<CartResponse> removeLine(@AuthenticationPrincipal UserDetails principal,
            @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String guestTokenHeader,
            @PathVariable UUID productId) {
        Cart cart = resolveCartForWrite(principal, guestTokenHeader);
        cart = cartService.removeLine(cart, productId);
        return ApiResponse.success(toResponse(cart));
    }

    @PostMapping("/merge")
    public ApiResponse<CartResponse> mergeGuestCart(@AuthenticationPrincipal UserDetails principal,
            @RequestHeader(name = GUEST_TOKEN_HEADER, required = false) String guestTokenHeader) {
        UUID userId = resolveUserId(principal);
        UUID guestToken = guestTokenHeader != null ? UUID.fromString(guestTokenHeader) : null;
        cartMergeService.mergeGuestCartIntoUserCart(guestToken, userId);
        Cart cart = cartService.resolveOrCreateActiveCart(userId, null);
        return ApiResponse.success(toResponse(cart));
    }

    private Cart resolveCartForWrite(UserDetails principal, String guestTokenHeader) {
        UUID userId = resolveUserId(principal);
        if (userId != null) {
            return cartService.resolveOrCreateActiveCart(userId, null);
        }
        UUID guestToken = guestTokenHeader != null ? UUID.fromString(guestTokenHeader) : UUID.randomUUID();
        return cartService.resolveOrCreateActiveCart(null, guestToken);
    }

    private UUID resolveUserId(UserDetails principal) {
        if (principal == null) {
            return null;
        }
        return currentUserQueryService.getByEmail(principal.getUsername()).getId();
    }

    private CartResponse toResponse(Cart cart) {
        List<UUID> productIds = cart.getItems().stream().map(CartItem::getProductId).toList();
        Map<UUID, ProductSummaryResponse> productSummariesByProductId = catalogQueryService
                .findProductSummariesByIds(productIds);
        return cartResponseMapper.toResponse(cart, productSummariesByProductId);
    }
}
