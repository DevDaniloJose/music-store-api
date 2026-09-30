package com.vadastore.music_store_api.record;

import com.vadastore.music_store_api.domain.OrderItem;
import com.vadastore.music_store_api.domain.Product;

import java.math.BigDecimal;

public record OrderItemResponse(Long productId, String productName, int quantity, BigDecimal unitPrice, BigDecimal subTotal) {


    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        return new OrderItemResponse(orderItem.getProduct().getId(), orderItem.getProduct().getName(), orderItem.getOrderAmount(), orderItem.getOrderItemUnitPrice(),
                orderItem.getOrderItemUnitPrice().multiply(BigDecimal.valueOf(orderItem.getOrderAmount())));
    }

}

