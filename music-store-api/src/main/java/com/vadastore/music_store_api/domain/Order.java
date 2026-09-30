package com.vadastore.music_store_api.domain;

import com.vadastore.music_store_api.enums.OrderStatus;
import com.vadastore.music_store_api.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import com.vadastore.music_store_api.enums.ShippingMethod;
import com.vadastore.music_store_api.enums.CouponCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "tb_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String userAddress;

    @Enumerated(EnumType.STRING)
    private CouponCode couponCode;

    private BigDecimal shippingFee;

    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    private ShippingMethod shippingMethod;

    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private LocalDateTime createdAt;


    public void addOrderItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }

}
