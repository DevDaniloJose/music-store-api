package com.vadastore.music_store_api.repository;


import com.vadastore.music_store_api.enums.Category;
import com.vadastore.music_store_api.domain.Product;
import com.vadastore.music_store_api.domain.Seller;
import com.vadastore.music_store_api.util.ProductCreator;
import com.vadastore.music_store_api.util.SellerCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@DataJpaTest
@Testcontainers
@DisplayName("Tests the Product Repository")
public class ITTestRepository {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Test
    @DisplayName("Should find product by sellerID")
    void shouldFindProductBySellerID() {
        Seller seller = SellerCreator.createSeller();
        Product product = ProductCreator.createProduct();
        product.setSeller(seller);

        sellerRepository.save(seller);
        productRepository.save(product);


        List<Product> productsByThatSeller = productRepository.findBySellerId(seller.getId());

        Assertions.assertThat(productsByThatSeller.getFirst().getSeller().getId()).isEqualTo(seller.getId());
    }

    @Test
    @DisplayName("Should return empty if sellerId notFound")
    void shouldReturnEmptyIfSellerIDNotFound() {
        Long id = 999L;
        Product product = ProductCreator.createProduct();
        productRepository.save(product);

        List<Product> emptyList = productRepository.findBySellerId(id);

        Assertions.assertThat(emptyList).isEmpty();
    }

    @Test
    @DisplayName("should find a product by category")
    void shouldFindAProjectByCategory() {
        Product product = ProductCreator.createProduct();

        productRepository.save(product);

        List<Product> products = productRepository.findByCategory(Category.SHIRT);

        Assertions.assertThat(products.getFirst().getCategory()).isEqualTo(Category.SHIRT);

    }

    @Test
    @DisplayName("Should find all products with stock greater than zero")
    void shouldFindAllProducts_WhichStockIsGreaterThanZero() {

        Product product = ProductCreator.createProduct();
        Product product2 = ProductCreator.createProduct();
            productRepository.save(product);
            productRepository.save(product2);

        List<Product> productsFound = productRepository.findByStockGreaterThan(0);

        Assertions.assertThat(productsFound.getFirst().getStock()).isGreaterThan(0);
    }

    @Test
    @DisplayName("should not list products which stock is <= 0")
    void shouldNotListProducts_WhichStocksIsLessOrEqualZero() {
        Product productNoStock = ProductCreator.createProductNoStock();
        productRepository.save(productNoStock);

        List<Product> productFound = productRepository.findByStockGreaterThan(0);

        Assertions.assertThat(productFound).isEmpty();
    }

    @Test
    @DisplayName("Should find product by id")
    void shouldFindProductById() {
        Product product = ProductCreator.createProduct();
        productRepository.save(product);

        Optional<Product> productFound = productRepository.findById(product.getId());

        Assertions.assertThat(productFound).isPresent();
        Assertions.assertThat(productFound.get().getName()).isEqualTo("Masayoshi Takanaka");
    }

    @Test
    @DisplayName("Should return empty optional if product not found")
    void shouldReturnEmptyOptional_IfProductNotFound() {
       Long id = 999L;

       Assertions.assertThat(productRepository.findProductById(id)).isEmpty();
    }


        @Test
    @DisplayName("Should return products by a specific price range")
    void shouldReturnProductsByAPriceRange() {
            Product product = ProductCreator.createProduct();
            product.setPrice(BigDecimal.valueOf(25));
            productRepository.save(product);
            BigDecimal minPrice = new BigDecimal(20);
            double maxPrice = 30;

            List<Product> productsFound = productRepository.findByPriceBetween(minPrice, BigDecimal.valueOf(maxPrice));
            Assertions.assertThat(productsFound).isNotEmpty();
            Assertions.assertThat(productsFound.getFirst().getPrice()).isEqualTo(25);
        }



}
