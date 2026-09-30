package com.vadastore.music_store_api.record;

import com.vadastore.music_store_api.enums.Category;
import com.vadastore.music_store_api.domain.Product;
import com.vadastore.music_store_api.domain.Seller;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(

                                @NotNull(message = "Name is required")
                             String name,
                             String description,


                             @NotNull(message = "Price is required")
                             @Positive(message = "Price must be positive")
                                BigDecimal price,
                             @PositiveOrZero(message = "Promotional price cannot be negative")
                             BigDecimal promotionalPrice,
                             @NotBlank(message = "SKU is required")
                             String SKU,

                             @NotNull(message = "Stock quantity is required :)")
                             @Min(value = 0, message = "Stock cannot be negative")
                             Integer  stockQuantity,

                             @Min(value = 0, message = "Min stock threshold cannot be negative")
                             Integer minStockThreshold,

                             @Positive(message = "Weight must be greater than zero")
                             Double weight,

                             @Positive(message = "height must be greater than zero")
                             Double height,
                                @Positive(message = "width must be greater than zero")
                                Double width,
                                @Positive(message = "length must be greater than zero")
                             Double length,
                             @NotNull(message = "Category is required")
                             Category category,
                             List<String> tags,
                             String mainImageUrl,
                             List<String> galleryImageUrls,

                             @NotNull(message = "isActive is required")
                             Boolean active) {


        public Product toEntity(Seller seller) {
            return Product.builder()
                    .name(this.name)
                    .price(this.price)
                    .description(this.description)
                    .promotionalPrice(this.promotionalPrice)
                    .SKU(this.SKU)
                    .minStockThreshold(this.minStockThreshold != null ? this.minStockThreshold : 5)
                    .weight(this.weight)
                    .height(this.height)
                    .width(this.width)
                    .length(this.length)
                    .category(this.category)
                    .tags(this.tags)
                    .mainImageUrl(this.mainImageUrl)
                    .galleryImageUrls(this.galleryImageUrls)
                    .stock(this.stockQuantity)
                    .isActive(this.active == null || this.active)
                    .seller(seller)
                    .build();
        }
        }
