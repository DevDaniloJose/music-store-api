package com.vadastore.music_store_api.record;

import com.vadastore.music_store_api.domain.Seller;
import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.enums.Category;
import com.vadastore.music_store_api.domain.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record ProductResponse(Long id, String name, String description,
                              BigDecimal price, BigDecimal promotionalPrice, String SKU,
                              Integer stockQuantity, Double weight, Double height,
                              Double width, Double length, Category category,
                              List<String> tags, String mainImageUrl, List<String> galleryImageUrls,
                              Boolean active, Long sellerId, String sellerName, LocalDateTime createdAt) {



    public static ProductResponse fromEntity(Product product) {

        Long sellerId = Optional.ofNullable(product.getSeller())
                .map(Seller::getId)
                .orElse(null);

        String username = Optional.ofNullable(product.getSeller())
                .map(Seller::getUser)
                .map(User::getUsername).orElse("SEller not specified");

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getPromotionalPrice(),
                product.getSKU(),
                product.getStock(),
                product.getWeight(),
                product.getHeight(),
                product.getWidth(),
                product.getLength(),
                product.getCategory(),
                product.getTags(),
                product.getMainImageUrl(),
                product.getGalleryImageUrls(),
                product.getIsActive(),
                sellerId,
               username,
                product.getCreatedAt()
        );
    }

}
