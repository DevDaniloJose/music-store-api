package com.vadastore.music_store_api.record;

import com.vadastore.music_store_api.domain.CartItem;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

public record CartItemResponse(Long itemId, ProductResponse product, int quantity, BigDecimal subTotal) {


        public static CartItemResponse fromEntity(CartItem cartItem) {
            BigDecimal unitPrice = cartItem.getProduct().getEffectivePrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));


           return new CartItemResponse(cartItem.getId(), ProductResponse.fromEntity(cartItem.getProduct()), cartItem.getQuantity(), subtotal);
        }

}
