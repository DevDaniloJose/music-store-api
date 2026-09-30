package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.domain.Seller;
import com.vadastore.music_store_api.repository.SellerRepository;
import com.vadastore.music_store_api.util.SellerCreator;
import com.vadastore.music_store_api.record.ProductRequest;
import com.vadastore.music_store_api.record.ProductResponse;
import com.vadastore.music_store_api.repository.ProductRepository;
import com.vadastore.music_store_api.domain.Product;
import com.vadastore.music_store_api.util.ProductCreator;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should list all available products")
    void shouldListAllAvailableProducts() {

        List<Product> productList = ProductCreator.createProductList();

        when(productRepository.findByStockGreaterThan(0)).thenReturn(productList);

        List<ProductResponse> products = productService.listAllAvailableProducts();


        Assertions.assertThat(products).isNotEmpty();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if list is empty")
    void shouldThrowIllegalArgumentExceptionIfListIsEmpty() {

        List<Product> emptyList = new ArrayList<>();

        when(productRepository.findByStockGreaterThan(0)).thenReturn(emptyList);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> productService.listAllAvailableProducts());
    }

    @Test
    @DisplayName("Should find the product by the id")
    void shouldFindTheProductByTheId() {
        Product product = ProductCreator.createProduct();

        when(productRepository.findProductById(product.getId())).thenReturn(Optional.of(product));

        ProductResponse productFound = productService.findProductById(product.getId());

        Assertions.assertThat(productFound.name()).isEqualTo("Masayoshi Takanaka");
        Assertions.assertThat(productFound.price()).isEqualTo(BigDecimal.valueOf(19.90));
        verify(productRepository, times(1)).findProductById(product.getId());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if product not found by id")
    void shouldThrowEntityNotFoundException_IfProductNotFoundById() {

        Long id = 999L;

        when(productRepository.findProductById(id)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(EntityNotFoundException.class, () -> productService.findProductById(id));
    }

    @Test
    @DisplayName("should save product on db")
    void shouldSaveProductOnDb() {
        ProductRequest product = ProductCreator.createProductRequest();
        Seller seller = SellerCreator.createSeller();

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        when(sellerRepository.findById(seller.getId())).thenReturn(Optional.of(seller));

        ProductResponse productSaved = productService.saveProduct(product, seller.getId());


        Assertions.assertThat(product).isNotNull();
        Assertions.assertThat(productSaved.name()).isEqualTo(product.name());
        Assertions.assertThat(productSaved.price()).isEqualByComparingTo(product.price());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productRepository, times(1)).save(productCaptor.capture());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if something is filled up wrong")
    void shouldThrowIllegalArgumentException_IfSomethingIsFilledUpWrong() {
        ProductRequest product = ProductCreator.createInvalidProductRequest();
        Seller seller = SellerCreator.createSeller();




        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> productService.saveProduct(product, seller.getId()));
        verify(productRepository, never()).save(any(Product.class));
    }

        @Test
        @DisplayName("Should edit a product that already exists on db")
        void shouldEditProductThatAlreadyExistsOnDB() {
            Product productBase = ProductCreator.createProduct();
            ProductRequest productUpdated = ProductCreator.createProductRequest();
            when(productRepository.findById(productBase.getId())).thenReturn(Optional.of(productBase));
            when(productRepository.save(productBase)).thenReturn(productBase);

           productService.editProduct(productBase.getId(), productUpdated);

            Assertions.assertThat(productBase.getPrice()).isEqualTo(productUpdated.price());
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            Product product = ProductCreator.createProduct();
            ProductRequest requestProductWithPartialFields = ProductCreator.createRequestProductWithPartialFields();
            String oldName = product.getName();

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            ProductResponse productResponse = productService.editProduct(product.getId(), requestProductWithPartialFields);

            Assertions.assertThat(product.getPrice()).isEqualTo(requestProductWithPartialFields.price());
            Assertions.assertThat(oldName).isEqualTo(productResponse.name());


        }

        @Test
        @DisplayName("Should decrease the stock amount if a product is bought")
    void shouldDecreaseStockAmountIfProductIsBought() {
            Product product = ProductCreator.createProduct();
            int initialStock = product.getStock();
            int quantityToBuy = 2;

            when(productRepository.findProductById(product.getId())).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);

            ProductResponse result = productService.buyProduct(product.getId(), quantityToBuy);


            Assertions.assertThat(result).isNotNull();

            int expectedStock = initialStock - quantityToBuy;

            Assertions.assertThat(result.stockQuantity()).isEqualTo(expectedStock);

            verify(productRepository, times(1)).save(product);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if buy amount is greater than stock")
        void shouldThrowIllegalArgumentException_IfBuyAmountIsGreaterThanStock() {
            Product product = ProductCreator.createProduct();
            int quantityToBuy = 15;

            when(productRepository.findProductById(product.getId())).thenReturn(Optional.of(product));

            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> productService.buyProduct(product.getId(), quantityToBuy));
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should find product by price range")
    void shouldFindProductByPriceRange() {
            List<Product> products = ProductCreator.createProductList();

            BigDecimal minPrice = new BigDecimal("19.90");
            BigDecimal maxPrice = new BigDecimal("39.90");

            when(productRepository.findByPriceBetween(minPrice, maxPrice)).thenReturn(products);


            List<ProductResponse> response = productService.findProductByPriceRange(minPrice, maxPrice);

            Assertions.assertThat(response.getFirst().price()).isEqualByComparingTo(new BigDecimal("19.90"));
            Assertions.assertThat(response).isNotNull();
            verify(productRepository, times(1)).findByPriceBetween(minPrice, maxPrice);
        }

        @ParameterizedTest
        @CsvSource({
                "-5, 20.0",
                "10, -2.5",
                "-2.0, -2.0"
        })
        @DisplayName("should throw IllegalArgumentException for various negative price inputs")
        void shouldThrowIllegalArgumentExceptionForVariousNegativePriceInputs(BigDecimal minPrice, BigDecimal maxPrice) {

            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> productService.findProductByPriceRange(minPrice, maxPrice));
            verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException if maxPrice < minPrice")
    void shouldThrowIllegalArgumentExceptionIf_MaxPriceLessTanMinPrice() {
        BigDecimal maxPrice = new BigDecimal("10.0");
        BigDecimal minPrice = new BigDecimal("15.0");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,() -> productService.findProductByPriceRange(minPrice, maxPrice));
        verify(productRepository, never()).save(any(Product.class));

    }
}
