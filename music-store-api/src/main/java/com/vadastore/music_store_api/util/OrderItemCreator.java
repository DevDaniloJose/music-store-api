package com.vadastore.music_store_api.util;
import com.vadastore.music_store_api.domain.Order;
import com.vadastore.music_store_api.domain.OrderItem;
import com.vadastore.music_store_api.domain.Product;

import java.math.BigDecimal;

public class OrderItemCreator {

    public static OrderItem orderItemCreator() {

        Product product = ProductCreator.createProduct();
        product.setId(1L);

        return OrderItem.builder()
                .order(null)
                .product(product)
                .orderAmount(10)
                .orderItemUnitPrice(new BigDecimal("19.90"))
                .build();
    }

}
