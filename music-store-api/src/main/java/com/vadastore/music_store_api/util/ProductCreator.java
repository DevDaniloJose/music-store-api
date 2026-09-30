package com.vadastore.music_store_api.util;

import com.vadastore.music_store_api.domain.Seller;
import com.vadastore.music_store_api.enums.Category;
import com.vadastore.music_store_api.domain.Product;
import com.vadastore.music_store_api.record.ProductRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class ProductCreator {

    public static Product createProduct() {
        return Product.builder()
                .name("Masayoshi Takanaka")
                .price(BigDecimal.valueOf(19.90))
                .stock(2)
                .description("really cool shirt")
                .category(Category.SHIRT)
                .seller(SellerCreator.createSeller())
                .build();
    }

    public static List<Product> createProductList() {
        Seller seller = SellerCreator.createSeller();


        return Collections.singletonList(Product.builder()
                .name("Masayoshi Takanaka")
                .price(BigDecimal.valueOf(19.90))
                .stock(2)
                .seller(seller)
                .description("really cool shirt")
                .category(Category.SHIRT)
                .build());
    }

    public static Product createProductNoStock() {
        return Product.builder()
                .name("Masayoshi Takanaka shirt")
                .price(BigDecimal.valueOf(19.90))
                .stock(0)
                .description("really cool shirt")
                .category(Category.SHIRT)
                .build();
    }

    public static ProductRequest createProductRequest() {
        return new ProductRequest(
                "Masayoshi Takanaka shirt",
                "really cool shirt",
                BigDecimal.valueOf(29.90),
                null,
                "TAKANAKA-SHIRT-01",
                2,
                5,
                0.5,
                1.0,
                1.0,
                1.0,
                Category.SHIRT,
                List.of("city-pop", "fusion"),
                "http://image.com/takanaka.jpg",
                Collections.emptyList(),
                true
        );
    }

    public static ProductRequest createInvalidProductRequest() {
        return new ProductRequest(
                "Masayoshi Takanaka shirt",
                "really cool shirt",
                BigDecimal.valueOf(19.90),
                null,
                "TAKANAKA-SHIRT-01",
                0,
                5,
                0.5,
                1.0,
                1.0,
                1.0,
                Category.SHIRT,
                List.of("city-pop", "fusion"),
                "http://image.com/takanaka.jpg",
                Collections.emptyList(),
                true
        );
    }

    public static ProductRequest createRequestProductWithPartialFields() {
        return new ProductRequest(
                null,
                null,
                BigDecimal.valueOf(39.90),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }


}
