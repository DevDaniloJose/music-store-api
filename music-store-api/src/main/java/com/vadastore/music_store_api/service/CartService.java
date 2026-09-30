package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.enums.CouponCode;
import com.vadastore.music_store_api.enums.OrderStatus;
import com.vadastore.music_store_api.exceptions.InsufficientStockException;
import com.vadastore.music_store_api.domain.*;
import com.vadastore.music_store_api.exceptions.ResourceNotFoundException;
import com.vadastore.music_store_api.record.*;
import com.vadastore.music_store_api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final AddressRepository addressRepository;
    private final OrderService orderService;

    public CartItemResponse addItem(CartItemRequest item, Long cartId) {
        Product product = productRepository.findProductById(item.productId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new EntityNotFoundException("cart not found"));
        Optional<CartItem> itemDb = cartItemRepository.findByProductIdAndCartId(item.productId(), cartId);

        int currentQuantity = itemDb.map(CartItem::getQuantity).orElse(0);
        int newAmount = currentQuantity + item.quantity();

        if (newAmount > product.getStock()) {
            throw new IllegalArgumentException("Unavailable stock");
        }

        if (!product.getIsActive()) {
            throw new IllegalArgumentException("product is not active");
        }

        CartItem cartItem;

        if (itemDb.isPresent()) {
            cartItem = itemDb.get();
            cartItem.setQuantity(newAmount);
        } else {
            cartItem = CartItem.builder().quantity(newAmount)
                    .cart(cart)
                    .product(product).build();
        }

        CartItem savedItem = cartItemRepository.save(cartItem);

        BigDecimal subTotal = savedItem.getProduct().getEffectivePrice().multiply(BigDecimal.valueOf(savedItem.getQuantity()));

        return new CartItemResponse(savedItem.getId(), ProductResponse.fromEntity(product),savedItem.getQuantity(), subTotal);
    }

    public void removeItem(Long productId, Long cartId) {
        CartItem cartItem = cartItemRepository.findByProductIdAndCartId(productId, cartId).orElseThrow(() -> new EntityNotFoundException("Cart item not found with productId: \" + productId + \" and cartId: \" + cartId"));
        cartItemRepository.delete(cartItem);
    }

    public CartItemResponse updateItemQuantity(Long productId, Long cartId, int newQuantity) {

        if (newQuantity <= 0) {
            removeItem(productId, cartId);
            return null;
        }

        CartItem cartItem = cartItemRepository.findByProductIdAndCartId(productId, cartId).orElseThrow(() -> new EntityNotFoundException("teste"));


        if (newQuantity > cartItem.getProduct().getStock()) {
            throw new IllegalArgumentException("Unavailable stock");
        }

        cartItem.setQuantity(newQuantity);

        CartItem savedItem = cartItemRepository.save(cartItem);


        return CartItemResponse.fromEntity(savedItem);
    }

    @Transactional
    public void clearCart(Long cartId) {
        cartItemRepository.deleteAllByCartId(cartId);
    }

    @Transactional()
    public CartResponse getCart(Long buyerId) {

        Cart cart = cartRepository.findByBuyerId(buyerId).orElseThrow(() -> new EntityNotFoundException("cart not found"));

        BigDecimal totalPrice = cart.getItems().stream().map(item -> item.getProduct().getEffectivePrice().multiply(BigDecimal.valueOf(item.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();

        List<CartItemResponse> items = cart.getItems().stream().map(CartItemResponse::fromEntity).toList();

        List<StockWarningResponse> stockWarningResponses = validStockResponse(cart.getId());



        return new CartResponse(cart.getId(), items, totalPrice, totalItems, stockWarningResponses);
    }

    public CartResponse applyCoupon(Long cartId, String couponCodeStr) {

        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new EntityNotFoundException("couldn't find cart"));
        CouponCode couponCodeEnum;
        try {
            couponCodeEnum = CouponCode.valueOf(couponCodeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
           throw new ResourceNotFoundException("invalid coupon");
        }

        BigDecimal discountFactor = couponCodeEnum.getDiscountFactor();
        cart.setCouponCode(couponCodeEnum);
        cart.setDiscountFactor(couponCodeEnum.getDiscountFactor());

        cart.setDiscountFactor(discountFactor);

        cartRepository.save(cart);

       return CartResponse.fromEntity(cart);
    }


    public List<StockWarningResponse> validStockResponse(Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        List<StockWarningResponse> warnings = new ArrayList<>();

            for (CartItem item : cart.getItems()) {

                if (item.getQuantity() > item.getProduct().getStock()) {
                    warnings.add(new StockWarningResponse(item.getId(),
                      item.getQuantity(), item.getProduct().getStock(), "Not available stock for this product:" + item.getProduct().getName()));
                }

            }

            return warnings;
    }


    @Transactional
    public CartResponse mergeGuestCartWithUserCart(Long guestCartId, Long cartId) {

        Cart userCart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        if (guestCartId.equals(cartId)) {
            return CartResponse.fromEntity(userCart);
        }

        Cart guestCart = cartRepository.findById(guestCartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        if (guestCart.getBuyer() != null) {
            throw new IllegalArgumentException("Invalid guest cart");
        }

        for (CartItem guestItem : guestCart.getItems()) {
            Product product = guestItem.getProduct();
            int availableStock = product.getStock();

            Optional<CartItem> existingItem = userCart.getItems().stream()
                    .filter(c -> c.getProduct().getId().equals(product.getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                CartItem userItem = existingItem.get();
                int totalWanted = userItem.getQuantity() + guestItem.getQuantity();
                userItem.setQuantity(Math.min(totalWanted, availableStock));
            } else {

                CartItem newItem = CartItem.builder()
                        .cart(userCart)
                        .product(product)
                        .quantity(Math.min(guestItem.getQuantity(), availableStock))
                        .build();
                userCart.getItems().add(newItem);
            }
        }

        cartRepository.save(userCart);
        cartRepository.delete(guestCart);

        return CartResponse.fromEntity(userCart);
    }


    @Transactional
    public OrderResponse checkout(Long buyerId, CheckoutRequest checkoutRequest) {
        return orderService.createOrderFromCheckout(buyerId, checkoutRequest);
    }
}
