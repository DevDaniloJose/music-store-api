package com.vadastore.music_store_api.util;
import com.vadastore.music_store_api.domain.Buyer;
import com.vadastore.music_store_api.domain.Order;
import com.vadastore.music_store_api.domain.OrderItem;
import com.vadastore.music_store_api.enums.CouponCode;
import com.vadastore.music_store_api.enums.OrderStatus;
import com.vadastore.music_store_api.enums.PaymentMethod;
import com.vadastore.music_store_api.enums.ShippingMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class OrderCreator {

    public static Order orderCreator() {
        OrderItem orderItem = OrderItemCreator.orderItemCreator();
        Buyer buyer = BuyerCreator.buyerCreator();

        return Order.builder()
                .id(1L).buyer(buyer)
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.PIX)
                .userAddress("flower street, 501").shippingFee(new BigDecimal("15.00")).shippingMethod(ShippingMethod.SEDEX)
                .totalPrice(new BigDecimal("199.00")).items(List.of(orderItem)).createdAt(LocalDateTime.now())
               .couponCode(CouponCode.VADA10)
               .discountAmount(new BigDecimal("0.90")).build();
    }

    public static List<Order> orderCreatorList() {
        OrderItem orderItem = OrderItemCreator.orderItemCreator();

        return Collections.singletonList(Order.builder()
                .id(1L).buyer(null)
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.PIX)
                .userAddress("flower street, 501").shippingFee(new BigDecimal("15.00")).shippingMethod(ShippingMethod.SEDEX)
                .totalPrice(new BigDecimal("199.00")).items(List.of(orderItem)).createdAt(LocalDateTime.now())
                .couponCode(CouponCode.VADA10)
                .discountAmount(new BigDecimal("0.90")).build());
    }

}
