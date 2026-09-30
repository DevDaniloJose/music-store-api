package com.vadastore.music_store_api.domain;

import com.vadastore.music_store_api.enums.CouponCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private CouponCode couponCode;

    @OneToOne
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    private BigDecimal discountFactor;



}
