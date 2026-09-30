package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.domain.*;
import com.vadastore.music_store_api.enums.OrderStatus;
import com.vadastore.music_store_api.exceptions.InsufficientStockException;
import com.vadastore.music_store_api.record.CheckoutRequest;
import com.vadastore.music_store_api.record.OrderItemRequest;
import com.vadastore.music_store_api.record.OrderItemResponse;
import com.vadastore.music_store_api.record.OrderResponse;
import com.vadastore.music_store_api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

        private final BuyerRepository buyerRepository;
        private final ProductRepository productRepository;
        private final ProductService productService;
        private final OrderRepository orderRepository;
        private final CartRepository cartRepository;
        private final AddressRepository addressRepository;
        private final CartItemRepository cartItemRepository;

        @Transactional
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
    public OrderResponse createOrderFromCheckout(Long buyerId, CheckoutRequest checkoutRequest) {
        Buyer buyer = buyerRepository.findBuyerById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found"));

        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for this buyer"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        Address address = addressRepository.findById(checkoutRequest.shippingAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        if (!address.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Address does not belong to this buyer");
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        Order order = Order.builder()
                .buyer(buyer)
                .orderStatus(OrderStatus.PENDING)
                .shippingMethod(checkoutRequest.shippingMethod())
                .paymentMethod(checkoutRequest.paymentMethod())
                .couponCode(cart.getCouponCode())
                .items(new ArrayList<>())
                .build();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (item.getQuantity() > product.getStock()) {
                throw new InsufficientStockException("Unavailable stock for product: " + product.getName());
            }

            productService.decreaseStock(product.getId(), item.getQuantity());

            BigDecimal unitPrice = product.getEffectivePrice();
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .orderAmount(item.getQuantity())
                    .orderItemUnitPrice(unitPrice)
                    .build();

            order.addOrderItem(orderItem);
        }

        BigDecimal finalPrice = subtotal;
        BigDecimal discountAmount = BigDecimal.ZERO;


        if (cart.getDiscountFactor() != null) {
            finalPrice = subtotal.multiply(cart.getDiscountFactor());
            discountAmount = subtotal.subtract(finalPrice);
        }

        finalPrice = finalPrice.setScale(2, RoundingMode.HALF_UP);
        discountAmount = discountAmount.setScale(2, RoundingMode.HALF_UP);

        String formattedAddress = String.format("%s, %s - %s, %s",
                address.getStreet(), address.getCity(), address.getState(), address.getZipCode());


        order.setTotalPrice(finalPrice);
        order.setDiscountAmount(discountAmount);
        order.setUserAddress(formattedAddress);

        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.setDiscountFactor(null);

        return OrderResponse.fromEntity(savedOrder);
    }

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING,   Set.of(OrderStatus.PAID),
            OrderStatus.PAID,      Set.of(OrderStatus.SENT),
            OrderStatus.SENT,      Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELED,  Set.of());

    private void validateTransition(OrderStatus from, OrderStatus to) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw new IllegalStateException("Cannot change order from " + from + " to " + to);
        }
    }

    @Transactional
    public OrderResponse markAsPaid(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        validateTransition(order.getOrderStatus(), OrderStatus.PAID);
        order.setOrderStatus(OrderStatus.PAID);
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Long buyerId, OrderStatus status) {
        if (status == OrderStatus.CANCELED) {
            throw new IllegalArgumentException("Use the cancel endpoint to cancel an order");
        }
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        validateTransition(order.getOrderStatus(), status);
        order.setOrderStatus(status);
        return OrderResponse.fromEntity(orderRepository.save(order));
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

