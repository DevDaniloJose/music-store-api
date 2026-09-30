package com.vadastore.music_store_api.domain;


import com.vadastore.music_store_api.enums.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal promotionalPrice;
    private String SKU;
    @Builder.Default
    private int minStockThreshold = 5;
    private Double weight;
    private Double height;
    private Double width;
    private Double length;
    private Boolean isActive;
    private LocalDateTime createdAt;

    @ElementCollection
    private List<String> tags = new ArrayList<>();


    @ElementCollection
    private List<String> galleryImageUrls = new ArrayList<>();


    private String mainImageUrl;
    private int stock;
    private String description;
    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    public BigDecimal getEffectivePrice() {
        if (this.promotionalPrice != null && promotionalPrice.compareTo(BigDecimal.ZERO) > 0) {
            return this.promotionalPrice;
        }

        return this.price;
    }

}
