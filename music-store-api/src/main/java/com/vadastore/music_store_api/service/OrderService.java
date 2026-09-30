package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.enums.OrderStatus;
import com.vadastore.music_store_api.domain.Buyer;
import com.vadastore.music_store_api.domain.Order;
import com.vadastore.music_store_api.domain.OrderItem;
import com.vadastore.music_store_api.domain.Product;
import com.vadastore.music_store_api.record.OrderItemRequest;
import com.vadastore.music_store_api.record.OrderItemResponse;
import com.vadastore.music_store_api.record.OrderResponse;
import com.vadastore.music_store_api.repository.BuyerRepository;
import com.vadastore.music_store_api.repository.OrderRepository;
import com.vadastore.music_store_api.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

        private final BuyerRepository buyerRepository;
        private final ProductRepository productRepository;
        private final ProductService productService;
        private final OrderRepository orderRepository;

    public OrderResponse createOrder(Long buyerId, List<OrderItemRequest> items) {
        Buyer buyer = buyerRepository.findBuyerById(buyerId).orElseThrow(() -> new EntityNotFoundException("Buyer not found"));


        Order order = Order.builder()
                .buyer(buyer)
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest item : items) {
            Product product = productRepository.findProductById(item.productId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));
            productService.decreaseStock(product.getId(), item.quantity());
            OrderItem orderItem = OrderItem.builder()
                    .product(product).orderAmount(item.quantity())
                    .orderItemUnitPrice(product.getPrice())
                    .build();

           totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
           order.addOrderItem(orderItem);
        }

        order.setTotalPrice(totalPrice);
      return OrderResponse.fromEntity(orderRepository.save(order));
    }


    @Transactional
        public OrderResponse markAsPaid(Long orderId, Long buyerId) {
            Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId).orElseThrow(() -> new EntityNotFoundException("Order not found"));

            if (order.getOrderStatus() == OrderStatus.CANCELED) {
                throw new IllegalStateException("Cannot pay a cancelled order");
            }

            if (order.getOrderStatus() == OrderStatus.PAID) {
                throw new IllegalStateException("This order has already been paid");
            }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("This order has already been paid and delivered");
        }

            order.setOrderStatus(OrderStatus.PAID);

           return OrderResponse.fromEntity(order);
        }


        @Transactional
        public OrderResponse cancelOrder(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId).orElseThrow(() -> new EntityNotFoundException("Order not found"));

            if (order.getOrderStatus() == OrderStatus.SENT) {
                throw new IllegalArgumentException("Cannot cancel an order that has already been sent");
            } else if (order.getOrderStatus() == OrderStatus.CANCELED) {
                throw new IllegalArgumentException("Cannot cancel an order that has already been cancelled");
            } else if (order.getOrderStatus() == OrderStatus.DELIVERED) {
                throw new IllegalArgumentException("Cannot cancel an order that has already been delivered");
            } else if (order.getOrderStatus() == OrderStatus.PAID) {
                throw new IllegalArgumentException("Cannot cancel an order that has already been paid");
            }

            for (OrderItem item : order.getItems()) {

                productService.restockProduct(item.getProduct().getId(), item.getOrderAmount());
            }

            order.setOrderStatus(OrderStatus.CANCELED);

        return OrderResponse.fromEntity(orderRepository.save(order));

        }

        public OrderResponse updateOrderStatus(Long orderId, Long buyerId, OrderStatus status) {
            Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId).orElseThrow(() -> new EntityNotFoundException("Order not found"));
             order.setOrderStatus(status);
          return OrderResponse.fromEntity( orderRepository.save(order));
    }

    public OrderResponse findOrderById(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId).orElseThrow(() -> new EntityNotFoundException("Order not found"));
        String buyerUsername = order.getBuyer().getUser().getUsername();
        String buyerEmail = order.getBuyer().getUser().getEmail();

        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> new OrderItemResponse(item.getProduct().getId(), item.getProduct().getName(), item.getOrderAmount(),
                item.getOrderItemUnitPrice(), item.getOrderItemUnitPrice().multiply(BigDecimal.valueOf(item.getOrderAmount())))).toList();

        return new OrderResponse(order.getId(),
                order.getBuyer().getId(),
                order.getCreatedAt(),
                order.getOrderStatus(), order.getTotalPrice(), buyerUsername, buyerEmail, order.getUserAddress(), itemResponses);
    }


    public List<OrderResponse> findOrdersByBuyer(Long buyerId) {
       buyerRepository.findBuyerById(buyerId).orElseThrow(() -> new EntityNotFoundException("Buyer not found"));

   return orderRepository.findByBuyerId(buyerId).stream().map(OrderResponse::fromEntity).toList();
    }

}

