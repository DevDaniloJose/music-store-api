package com.vadastore.music_store_api.controller;

import com.vadastore.music_store_api.domain.Buyer;
import com.vadastore.music_store_api.domain.Cart;
import com.vadastore.music_store_api.repository.BuyerRepository;
import com.vadastore.music_store_api.service.BuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.vadastore.music_store_api.record.CartResponse;
import jakarta.persistence.EntityNotFoundException;
import com.vadastore.music_store_api.repository.CartRepository;
import com.vadastore.music_store_api.security.UserDetailsImpl;
import com.vadastore.music_store_api.service.CartService;
import org.springframework.http.HttpStatus;
import com.vadastore.music_store_api.record.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartRepository cartRepository;

    private final CartService cartService;

    private final BuyerRepository buyerRepository;

    private final BuyerService buyerService;

    @GetMapping()
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetailsImpl user) {
        return new ResponseEntity<>(cartService.getCart(user.getId()), HttpStatus.OK);
    }


    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItem(@Valid @RequestBody CartItemRequest product, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long cartId = buyerService.findBuyerByUserId(userDetails.getUser().getId()).getCart().getId();
        return new ResponseEntity<>(cartService.addItem(product, cartId), HttpStatus.OK);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<String> removeItem(@PathVariable Long productId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long cartId = buyerService.findBuyerByUserId(userDetails.getUser().getId()).getCart().getId();
        cartService.removeItem(productId, cartId);
      return ResponseEntity.ok("Item successfully deleted");
    }

    @PutMapping("/updateItemQuantity/{productId}/{newQuantity}")
    public ResponseEntity<CartItemResponse> updateItemQuantity(@PathVariable Long productId, @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable int newQuantity) {
        Long cartId = buyerService.findBuyerByUserId(userDetails.getUser().getId()).getCart().getId();
        return new ResponseEntity<>(cartService.updateItemQuantity(productId, cartId, newQuantity), HttpStatus.OK);
    }

    @DeleteMapping("/deleteAllCartItems")
    public ResponseEntity<String> deleteAllCartItems(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long cartId = buyerService.findBuyerByUserId(userDetails.getUser().getId()).getCart().getId();
        cartService.clearCart(cartId);
        return ResponseEntity.ok("Cart successfully cleaned");
    }

    @PostMapping("/applyCoupon")
    public ResponseEntity<CartResponse> applyCoupon(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody ApplyCouponRequest couponCode) {
        Long cartId = buyerService.findBuyerByUserId(userDetails.getUser().getId()).getCart().getId();
        return new ResponseEntity<>(cartService.applyCoupon(cartId, couponCode.coupon()), HttpStatus.OK);
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody CheckoutRequest checkoutRequest) {
        Long cartId = buyerService.findBuyerByUserId(userDetails.getUser().getId()).getCart().getId();
        return new ResponseEntity<>(cartService.checkout(cartId, checkoutRequest), HttpStatus.CREATED);
    }

}
