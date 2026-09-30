package com.vadastore.music_store_api.service;


import com.vadastore.music_store_api.domain.Buyer;
import com.vadastore.music_store_api.domain.Order;
import com.vadastore.music_store_api.domain.OrderItem;
import com.vadastore.music_store_api.domain.Product;
import com.vadastore.music_store_api.enums.OrderStatus;
import com.vadastore.music_store_api.record.OrderItemRequest;
import com.vadastore.music_store_api.record.OrderResponse;
import com.vadastore.music_store_api.repository.BuyerRepository;
import com.vadastore.music_store_api.repository.OrderRepository;
import com.vadastore.music_store_api.repository.ProductRepository;
import com.vadastore.music_store_api.util.BuyerCreator;
import com.vadastore.music_store_api.util.OrderCreator;
import com.vadastore.music_store_api.util.OrderItemCreator;
import com.vadastore.music_store_api.util.ProductCreator;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.hibernate.mapping.Any;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BuyerRepository buyerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;


    @Test
    @DisplayName("should create order")
    void shouldCreateOrder() {
        Buyer buyer = BuyerCreator.buyerCreator();
        Product product = ProductCreator.createProduct();

        when(buyerRepository.findBuyerById(buyer.getId())).thenReturn(Optional.of(buyer));
        when(productRepository.findProductById(product.getId())).thenReturn(Optional.of(product));

        List<OrderItemRequest> orderItemRequest = Collections.singletonList(new OrderItemRequest(product.getId(), 2));

        OrderResponse order = orderService.createOrder(buyer.getId(), orderItemRequest);

        Assertions.assertThat(order.orderStatus()).isEqualTo(OrderStatus.PENDING);
    }


        @Test
        @DisplayName("should change order status to PAID if order is found")
        void shouldChangeOrderStatusToPaid_If_OrderIsFound() {
            Order order = OrderCreator.orderCreator();
            Buyer buyer = BuyerCreator.buyerCreator();

            when(orderRepository.findByIdAndBuyerId(order.getId(), buyer.getId())).thenReturn(Optional.of(order));
            OrderResponse orderResponse = orderService.markAsPaid(order.getId(), buyer.getId());

            Assertions.assertThat(orderResponse.orderStatus()).isEqualTo(OrderStatus.PAID);
            verify(orderRepository, times(1)).findByIdAndBuyerId(order.getId(), buyer.getId());
        }

        @Test
        @DisplayName("Should throw IllegalStateException if trying to change order status from CANCELED to PAID")
    void shouldThrowIllegalStateException_If_TryingToChangeFromCanceled_TO_Paid() {
            Order order = OrderCreator.orderCreator();
            order.setOrderStatus(OrderStatus.CANCELED);
            Buyer buyer = BuyerCreator.buyerCreator();
            when(orderRepository.findByIdAndBuyerId(order.getId(), buyer.getId())).thenReturn(Optional.of(order));


            org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> orderService.markAsPaid(order.getId(), buyer.getId()));
        }

    @Test
    @DisplayName("Should throw IllegalStateException if trying to change order status from PAID to PAID")
    void shouldThrowIllegalStateException_If_TryingToChangeFromPaid_TO_Paid() {
        Order order = OrderCreator.orderCreator();
        order.setOrderStatus(OrderStatus.PAID);
        Buyer buyer = BuyerCreator.buyerCreator();
        when(orderRepository.findByIdAndBuyerId(order.getId(), buyer.getId())).thenReturn(Optional.of(order));


        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> orderService.markAsPaid(order.getId(), buyer.getId()));
    }

    @Test
    @DisplayName("Should throw IllegalStateException if trying to change order status from DELIVERED to PAID")
    void shouldThrowIllegalStateException_If_TryingToChangeFromDelivered_TO_Paid() {
        Order order = OrderCreator.orderCreator();
        order.setOrderStatus(OrderStatus.DELIVERED);
        Buyer buyer = BuyerCreator.buyerCreator();
        when(orderRepository.findByIdAndBuyerId(order.getId(), buyer.getId())).thenReturn(Optional.of(order));


        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> orderService.markAsPaid(order.getId(), buyer.getId()));
    }

    @Test
    @DisplayName("Should throw Entity not found exception if order not found in DB")
    void shouldThrowEntityNotFoundException_If_OrderNotFound() {

        when(orderRepository.findByIdAndBuyerId(10L, 15L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(EntityNotFoundException.class, () -> orderService.markAsPaid(10L, 15L));
    }

    @Test
    @DisplayName("should cancel order if found")
    void shouldCancelOrderIfFound() {
        Order order = OrderCreator.orderCreator();
        Buyer buyer = BuyerCreator.buyerCreator();
        when(orderRepository.findByIdAndBuyerId(order.getId(), buyer.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        orderService.cancelOrder(order.getId(), buyer.getId());

        verify(orderRepository, times(1)).findByIdAndBuyerId(order.getId(), buyer.getId());
        verify(orderRepository, times(1)).save(order);
        verify(productService, times(order.getItems().size())).restockProduct(anyLong(), anyInt());
        Assertions.assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if order or buyer not found")
    void shouldThrowEntityNotFoundException_If_NoOrOrderFound() {

        when(orderRepository.findByIdAndBuyerId(10L, 15L)).thenReturn(Optional.empty());


        verify(orderRepository, times(0)).save(any(Order.class));
        org.junit.jupiter.api.Assertions.assertThrows(EntityNotFoundException.class, () -> orderService.cancelOrder(10L, 15L));
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"SENT", "CANCELED", "DELIVERED", "PAID"})
    @DisplayName("should throw IllegalArgumentException if trying to cancel an order that has already been sent")
    void shouldThrowIllegalArgumentException_If_TryingToCancelSentOrder(OrderStatus status) {
        Order order = OrderCreator.orderCreator();
        order.setOrderStatus(status);
        order.setOrderStatus(OrderStatus.SENT);
        Buyer buyer = BuyerCreator.buyerCreator();

        when(orderRepository.findByIdAndBuyerId(order.getId(), buyer.getId())).thenReturn(Optional.of(order));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.cancelOrder(order.getId(), buyer.getId()));
    }

    @Test
    @DisplayName("should update order status")
    void shouldUpdateOrderStatus() {
        Order order = OrderCreator.orderCreator();
        Buyer buyer = BuyerCreator.buyerCreator();
        when(orderRepository.findByIdAndBuyerId(order.getId(), buyer.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        OrderResponse orderResponse = orderService.updateOrderStatus(order.getId(), buyer.getId(), OrderStatus.PAID);

        Assertions.assertThat(order.getOrderStatus()).isEqualTo(orderResponse.orderStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("should not update anything if order or buyer not found")
    void shouldNotUpdateIf_OrderOrBuyerNotFound() {

        when(orderRepository.findByIdAndBuyerId(10L, 15L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(EntityNotFoundException.class, () -> orderService.updateOrderStatus(10L, 15L, OrderStatus.PAID));
    }

    @Test
    @DisplayName("should find order by order id")
    void shouldFindOrderById() {
        Order order = OrderCreator.orderCreator();
        Buyer buyer = BuyerCreator.buyerCreator();

        when(orderRepository.findByIdAndBuyerId(order.getId(), buyer.getId())).thenReturn(Optional.of(order));
        OrderResponse response = orderService.findOrderById(order.getId(), buyer.getId());

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.orderId()).isEqualTo(order.getId());
        Assertions.assertThat(response.buyerId()).isEqualTo(buyer.getId());
        Assertions.assertThat(response.orderStatus()).isEqualTo(order.getOrderStatus());
        Assertions.assertThat(response.totalPrice()).isEqualTo(order.getTotalPrice());
        Assertions.assertThat(response.items().size()).isEqualTo(order.getItems().size());

        verify(orderRepository, times(1)).findByIdAndBuyerId(order.getId(), buyer.getId());
    }

    @Test
    @DisplayName("should throw entityNotFoundException if order or buyer not found")
    void shouldThrowEntityNotFoundException_If_OrderOrBuyerNotFound() {


        when(orderRepository.findByIdAndBuyerId(10L, 15L)).thenReturn(Optional.empty());


        org.junit.jupiter.api.Assertions.assertThrows(EntityNotFoundException.class, () -> orderService.findOrderById(10L, 15L));
    }

    @Test
    @DisplayName("should find order by buyer id")
    void shouldFindOrderByBuyerId() {
        Buyer buyer = BuyerCreator.buyerCreator();

        when(buyerRepository.findBuyerById(buyer.getId())).thenReturn(Optional.of(buyer));

        List<OrderResponse> response = orderService.findOrdersByBuyer(buyer.getId());


        Assertions.assertThat(buyer.getCart().getItems().size()).isEqualTo(response.size());
        verify(buyerRepository, times(1)).findBuyerById(buyer.getId());
    }

    @Test
    @DisplayName("should throw EntityNotFoundException if buyer not found")
    void shouldThrowEntityNotFoundExceptionIfBuyerNotFound() {
        when(buyerRepository.findBuyerById(1L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(EntityNotFoundException.class, () -> orderService.findOrdersByBuyer(1L));

    }

}
