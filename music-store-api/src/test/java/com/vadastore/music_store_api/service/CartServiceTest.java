package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.domain.*;
import com.vadastore.music_store_api.enums.OrderStatus;
import com.vadastore.music_store_api.record.*;
import com.vadastore.music_store_api.repository.CartItemRepository;
import com.vadastore.music_store_api.repository.CartRepository;
import com.vadastore.music_store_api.repository.ProductRepository;
import com.vadastore.music_store_api.util.CartCreator;
import com.vadastore.music_store_api.util.OrderCreator;
import com.vadastore.music_store_api.util.OrderItemCreator;
import com.vadastore.music_store_api.util.ProductCreator;
import com.vadastore.music_store_api.enums.CouponCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vadastore.music_store_api.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;


    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;


    @InjectMocks
    private CartService cartService;


    @Test
    @DisplayName("Should add an item to the cart")
    void shouldAddItemToCart() {
        Product product = ProductCreator.createProduct();
        Cart cart = CartCreator.createCart();
        CartItem cartItem = CartCreator.createCartItem();
        CartItemRequest request = new CartItemRequest(product.getId(), cartItem.getQuantity());

        when(productRepository.findProductById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByProductIdAndCartId(product.getId(), cart.getId())).thenReturn(Optional.of(cartItem));

        CartItemResponse response = cartService.addItem(request, cart.getId());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(request.productId(), response.product().id());

        int expectedQuantity = cartItem.getQuantity() + request.quantity();
        Assertions.assertEquals(expectedQuantity, response.quantity());

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("should remove an item from cart")
    void shouldRemoveItemFromCart() {

        Product product = ProductCreator.createProduct();
        Cart cart = CartCreator.createCart();
        CartItem cartItem = CartCreator.createCartItem();


        when(cartItemRepository.findByProductIdAndCartId(product.getId(), cart.getId())).thenReturn(Optional.of(cartItem));

        cartService.removeItem(product.getId(), cart.getId());

        verify(cartItemRepository, times(1)).delete(cartItem);
    }


    @Test
    @DisplayName("should throw EntityNotFoundException when cart item does not exist")
    void shouldThrowExceptionWhenItemNotFound() {
        when(cartItemRepository.findByProductIdAndCartId(1L, 1L)).thenReturn(Optional.empty());
        verify(cartItemRepository, never()).delete(any());
    }


    @Test
    @DisplayName("should update item amount on cart")
    void shouldUpdateItemAmountOnCart() {
        Product product = ProductCreator.createProduct();
        Cart cart = CartCreator.createCart();
        CartItem cartItem = CartCreator.createCartItem();
        int newQuantity = 3;
        when(cartItemRepository.findByProductIdAndCartId(product.getId(), cart.getId())).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        CartItemResponse response = cartService.updateItemQuantity(product.getId(), cart.getId(), newQuantity);

        org.assertj.core.api.Assertions.assertThat(response.quantity()).isEqualTo(newQuantity);
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("Should clear cart")
    void shouldClearCart() {
        Cart cart = CartCreator.createCart();

        cartService.clearCart(cart.getId());

        verify(cartItemRepository).deleteAllByCartId(cart.getId());
    }

    @Test
    @DisplayName("should return the cart by id")
    void shouldReturnCartById() {
        Cart cart = CartCreator.createCart();

        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

        CartResponse cartResponse = cartService.getCart(cart.getId());

        org.assertj.core.api.Assertions.assertThat(cartResponse.totalItems()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(cartResponse.totalPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
    }


    @Test
    @DisplayName("should apply coupon to cart")
    void shouldApplyCouponToCart() {

        Cart cart = CartCreator.createCart();
        String couponCode = CouponCode.VADA10.name();

        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse cartResponse = cartService.applyCoupon(cart.getId(), couponCode);

        org.assertj.core.api.Assertions.assertThat(cartResponse.totalPrice()).isEqualByComparingTo(new BigDecimal("179.10"));
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException if coupon is invalid")
    void shouldThrowIllegalArgumentException_If_InvalidCoupon() {
        Cart cart = CartCreator.createCart();
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

        String coupon = "Invalid_coupon";


        Assertions.assertThrows(IllegalArgumentException.class, () -> cartService.applyCoupon(cart.getId(), coupon));

        verify(cartRepository, never()).save(cart);
    }

    @Test
    @DisplayName("should return stock warnings when item quantity exceeds available stock")
        void shouldReturnStockWarnings_WhenInsufficientStock() {
        Cart cart = CartCreator.createCartWithItemQuantityGreaterThanStock();

        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));


        List<StockWarningResponse> stockWarningResponses = cartService.validStockResponse(cart.getId());

        org.assertj.core.api.Assertions.assertThat(stockWarningResponses).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(stockWarningResponses).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(stockWarningResponses.getFirst().cartItemId()).isEqualTo(cart.getItems().getFirst().getId());
    }

    @Test
    @DisplayName("should return an empty list when item quantity does not exceed available stock")
    void shouldReturnEmptyList_WhenEverythingIsAlright() {
        Cart cart = CartCreator.createCart();

        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));


        List<StockWarningResponse> stockWarningResponses = cartService.validStockResponse(cart.getId());

        org.assertj.core.api.Assertions.assertThat(stockWarningResponses).isEmpty();
        org.assertj.core.api.Assertions.assertThat(stockWarningResponses).hasSize(0);
        verify(cartRepository, times(1)).findById(cart.getId());
    }

    @Test
    @DisplayName("should merge guest cart with userCart")
    void shouldMergeGuestCartWithUserCart() {
        Cart userCart = CartCreator.createCart();
        Cart guestCart = CartCreator.createGuestCart();


        when(cartRepository.findById(userCart.getId())).thenReturn(Optional.of(userCart));
        when(cartRepository.findById(guestCart.getId())).thenReturn(Optional.of(guestCart));
        when(cartRepository.save(userCart)).thenReturn(userCart);

        cartService.mergeGuestCartWithUserCart(guestCart.getId(), userCart.getId());

        org.assertj.core.api.Assertions.assertThat(userCart.getItems().getFirst().getQuantity()).isEqualTo(4);
        verify(cartRepository, times(1)).save(userCart);
        verify(cartRepository, times(1)).delete(guestCart);
    }

    @Test
    @DisplayName("should give the client a checkout of that order")
    void shouldGiveClientCheckout() {
        Cart cart = CartCreator.createCart();
        Order order = OrderCreator.orderCreator();
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        CheckoutRequest checkoutRequest = new CheckoutRequest(order.getPaymentMethod(), order.getShippingMethod(), order.getCouponCode(), "hehe", 1L);

        OrderResponse response = cartService.checkout(cart.getId(), checkoutRequest);

        org.assertj.core.api.Assertions.assertThat(response).isNotNull();
        org.assertj.core.api.Assertions.assertThat(response.orderStatus()).isEqualTo(OrderStatus.PENDING);
        org.assertj.core.api.Assertions.assertThat(response.totalPrice()).isEqualByComparingTo(order.getTotalPrice());
        org.assertj.core.api.Assertions.assertThat(response.items()).hasSize(cart.getItems().size());


        verify(orderRepository, times(1)).save(any(Order.class));
    }



}
