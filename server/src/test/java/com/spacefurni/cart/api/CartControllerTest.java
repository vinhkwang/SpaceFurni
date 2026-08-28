package com.spacefurni.cart.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.cart.api.dto.CartResponse;
import com.spacefurni.cart.api.mapper.CartResponseMapper;
import com.spacefurni.cart.application.CartMergeService;
import com.spacefurni.cart.application.CartService;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.catalog.application.CatalogQueryService;
import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.identity.security.SecurityConfiguration;
import com.spacefurni.identity.security.SpaceFurniUserDetailsService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = CartController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class CartControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvcWithSecurity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CartMergeService cartMergeService;

    @MockitoBean
    private CatalogQueryService catalogQueryService;

    @MockitoBean
    private CartResponseMapper cartResponseMapper;

    @MockitoBean
    private CurrentUserQueryService currentUserQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    private Cart mockCart() {
        Cart cart = mock(Cart.class);
        when(cart.getItems()).thenReturn(Set.of());
        return cart;
    }

    @Test
    void currentCartAsAnonymousWithoutGuestTokenReturnsEmptyCartWithoutTouchingCartService() throws Exception {
        mockMvc.perform(get("/api/v1/cart")).andExpect(status().isOk()).andExpect(jsonPath("$.data.id").isEmpty())
                .andExpect(jsonPath("$.data.lines").isEmpty())
                .andExpect(jsonPath("$.data.subtotalAmount").value(0));

        verifyNoInteractions(cartService);
    }

    @Test
    void currentCartAsAnonymousWithGuestTokenResolvesExistingCart() throws Exception {
        UUID guestToken = UUID.randomUUID();
        Cart cart = mockCart();
        when(cartService.resolveOrCreateActiveCart(null, guestToken)).thenReturn(cart);
        when(catalogQueryService.findProductSummariesByIds(List.of())).thenReturn(Map.of());
        when(cartResponseMapper.toResponse(cart, Map.of()))
                .thenReturn(new CartResponse(UUID.randomUUID(), guestToken, List.of(), 0L, "VND"));

        mockMvc.perform(get("/api/v1/cart").header("X-Guest-Token", guestToken.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.guestToken").value(guestToken.toString()));

        verify(cartService).resolveOrCreateActiveCart(null, guestToken);
    }

    @Test
    @WithMockUser(username = "user@spacefurni.com")
    void currentCartAsAuthenticatedUserResolvesUserCart() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(currentUserQueryService.getByEmail("user@spacefurni.com")).thenReturn(user);
        Cart cart = mockCart();
        when(cartService.resolveOrCreateActiveCart(userId, null)).thenReturn(cart);
        when(catalogQueryService.findProductSummariesByIds(List.of())).thenReturn(Map.of());
        when(cartResponseMapper.toResponse(cart, Map.of()))
                .thenReturn(new CartResponse(UUID.randomUUID(), null, List.of(), 0L, "VND"));

        mockMvc.perform(get("/api/v1/cart")).andExpect(status().isOk());

        verify(cartService).resolveOrCreateActiveCart(userId, null);
    }

    @Test
    void addLineAsAnonymousWithoutGuestTokenMintsNewToken() throws Exception {
        Cart cart = mockCart();
        when(cartService.resolveOrCreateActiveCart(isNull(), any())).thenReturn(cart);
        when(cartService.addLine(eq(cart), any(), eq(2))).thenReturn(cart);
        when(catalogQueryService.findProductSummariesByIds(List.of())).thenReturn(Map.of());
        when(cartResponseMapper.toResponse(cart, Map.of()))
                .thenReturn(new CartResponse(UUID.randomUUID(), UUID.randomUUID(), List.of(), 0L, "VND"));
        UUID productId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/cart/items").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"productId\": \"" + productId + "\", \"quantity\": 2 }"))
                .andExpect(status().isOk());

        ArgumentCaptor<UUID> mintedTokenCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(cartService).resolveOrCreateActiveCart(isNull(), mintedTokenCaptor.capture());
        assertThat(mintedTokenCaptor.getValue()).isNotNull();
        verify(cartService).addLine(cart, productId, 2);
    }

    @Test
    void addLineAsAnonymousWithExistingGuestTokenReusesIt() throws Exception {
        UUID guestToken = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Cart cart = mockCart();
        when(cartService.resolveOrCreateActiveCart(null, guestToken)).thenReturn(cart);
        when(cartService.addLine(cart, productId, 1)).thenReturn(cart);
        when(catalogQueryService.findProductSummariesByIds(List.of())).thenReturn(Map.of());
        when(cartResponseMapper.toResponse(cart, Map.of()))
                .thenReturn(new CartResponse(UUID.randomUUID(), guestToken, List.of(), 0L, "VND"));

        mockMvc.perform(post("/api/v1/cart/items").with(csrf()).header("X-Guest-Token", guestToken.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"productId\": \"" + productId + "\", \"quantity\": 1 }"))
                .andExpect(status().isOk());

        verify(cartService).resolveOrCreateActiveCart(null, guestToken);
        verify(cartService).addLine(cart, productId, 1);
    }

    @Test
    void addLineRejectsMissingProductId() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"quantity\": 1 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void addLineRejectsNonPositiveQuantity() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"productId\": \"" + UUID.randomUUID() + "\", \"quantity\": 0 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void updateLineQuantityDelegatesToService() throws Exception {
        UUID guestToken = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Cart cart = mockCart();
        when(cartService.resolveOrCreateActiveCart(null, guestToken)).thenReturn(cart);
        when(cartService.updateLineQuantity(cart, productId, 5)).thenReturn(cart);
        when(catalogQueryService.findProductSummariesByIds(List.of())).thenReturn(Map.of());
        when(cartResponseMapper.toResponse(cart, Map.of()))
                .thenReturn(new CartResponse(UUID.randomUUID(), guestToken, List.of(), 0L, "VND"));

        mockMvc.perform(patch("/api/v1/cart/items/" + productId).with(csrf())
                        .header("X-Guest-Token", guestToken.toString()).contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"quantity\": 5 }"))
                .andExpect(status().isOk());

        verify(cartService).updateLineQuantity(cart, productId, 5);
    }

    @Test
    void updateLineQuantityRejectsNegativeQuantity() throws Exception {
        mockMvc.perform(patch("/api/v1/cart/items/" + UUID.randomUUID()).with(csrf())
                        .header("X-Guest-Token", UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"quantity\": -1 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void removeLineDelegatesToService() throws Exception {
        UUID guestToken = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Cart cart = mockCart();
        when(cartService.resolveOrCreateActiveCart(null, guestToken)).thenReturn(cart);
        when(cartService.removeLine(cart, productId)).thenReturn(cart);
        when(catalogQueryService.findProductSummariesByIds(List.of())).thenReturn(Map.of());
        when(cartResponseMapper.toResponse(cart, Map.of()))
                .thenReturn(new CartResponse(UUID.randomUUID(), guestToken, List.of(), 0L, "VND"));

        mockMvc.perform(delete("/api/v1/cart/items/" + productId).with(csrf())
                        .header("X-Guest-Token", guestToken.toString()))
                .andExpect(status().isOk());

        verify(cartService).removeLine(cart, productId);
    }

    @Test
    void mergeGuestCartIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/cart/merge").with(csrf())
                        .header("X-Guest-Token", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@spacefurni.com")
    void mergeGuestCartDelegatesToMergeServiceThenReturnsUserCart() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID guestToken = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(currentUserQueryService.getByEmail("user@spacefurni.com")).thenReturn(user);
        Cart cart = mockCart();
        when(cartService.resolveOrCreateActiveCart(userId, null)).thenReturn(cart);
        when(catalogQueryService.findProductSummariesByIds(List.of())).thenReturn(Map.of());
        when(cartResponseMapper.toResponse(cart, Map.of()))
                .thenReturn(new CartResponse(UUID.randomUUID(), null, List.of(), 0L, "VND"));

        mockMvc.perform(post("/api/v1/cart/merge").with(csrf()).header("X-Guest-Token", guestToken.toString()))
                .andExpect(status().isOk());

        verify(cartMergeService).mergeGuestCartIntoUserCart(guestToken, userId);
    }
}
