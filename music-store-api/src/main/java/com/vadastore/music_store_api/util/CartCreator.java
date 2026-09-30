package com.vadastore.music_store_api.util;

import com.vadastore.music_store_api.domain.Buyer;
import com.vadastore.music_store_api.domain.Cart;
import com.vadastore.music_store_api.domain.CartItem;
import com.vadastore.music_store_api.domain.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartCreator {

    public static Cart createCart() {

        return Cart.builder()
                .id(1L)
                .buyer(BuyerCreator.buyerCreator())
                .items(List.of(createCartItem())).discountFactor(null).build();

    }

    public static Cart createGuestCart() {

        return Cart.builder()
                .id(2L)
                .items(List.of(createCartItem())).discountFactor(null).build();

    }

    public static CartItem createCartItem() {
       Product product = new Product();

       product.setId(1L);
       product.setPrice(new BigDecimal("19.90"));
        product.setStock(10);
       CartItem item = new CartItem();
       item.setId(1L);
       item.setProduct(product);
       item.setQuantity(10);
       return item;
    }

    public static CartItem createCartItemUnavailableStock() {
        Product product = new Product();

        product.setId(1L);
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(11);
        return item;
    }

    public static Cart createCartWithItemQuantityGreaterThanStock() {
        return Cart.builder()
                .id(1L)
                .items(List.of(createCartItemUnavailableStock())).discountFactor(null).build();
    }
}
