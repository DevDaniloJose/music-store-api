package com.vadastore.music_store_api.record;

import com.vadastore.music_store_api.enums.OrderStatus;
import com.vadastore.music_store_api.domain.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long orderId,
                            Long buyerId,
                            LocalDateTime createdAt,
                            OrderStatus orderStatus,
                            BigDecimal totalPrice,
                            String buyerName,
                            String buyerEmail,
                            String shippingAddress,
                            List<OrderItemResponse> items) {

    public static OrderResponse fromEntity(Order order) {
       return new OrderResponse(
               order.getId(), order.getBuyer().getId(), order.getCreatedAt(), order.getOrderStatus(),
               order.getTotalPrice(), order.getBuyer().getUser().getUsername(), order.getBuyer().getUser().getEmail(),
               order.getUserAddress(), order.getItems().stream().map(OrderItemResponse::fromEntity).toList()
       );
    }

}
