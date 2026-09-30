package com.vadastore.music_store_api.record;

import com.vadastore.music_store_api.domain.Cart;
import com.vadastore.music_store_api.domain.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record CartResponse(Long cartId, List<CartItemResponse> items, BigDecimal totalPrice, int totalItems, List<StockWarningResponse> warnings) {


    public static CartResponse fromEntity(Cart cart) {

        List<CartItemResponse> items = cart.getItems().stream().map(CartItemResponse::fromEntity).toList();

        BigDecimal totalPrice = cart.getItems().stream().map(item -> item.getProduct().getEffectivePrice().multiply(BigDecimal.valueOf(item.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (cart.getDiscountPercentage() != null) {
            totalPrice = totalPrice.multiply(cart.getDiscountPercentage());
        }

        int totalItems = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();

        List<StockWarningResponse> warnings = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            if (item.getQuantity() > item.getProduct().getStock()) {
               warnings.add(new StockWarningResponse(item.getId(), item.getQuantity(), item.getProduct().getStock(), "stock unavailable for the amount you are trying to add"));
            }
        }

        return new CartResponse(cart.getId(), items, totalPrice, totalItems, warnings);
    }

}
