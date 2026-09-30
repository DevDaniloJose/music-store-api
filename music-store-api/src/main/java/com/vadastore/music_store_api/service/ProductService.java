package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.record.ProductRequest;
import com.vadastore.music_store_api.record.ProductResponse;
import com.vadastore.music_store_api.repository.ProductRepository;
import com.vadastore.music_store_api.repository.SellerRepository;
import com.vadastore.music_store_api.domain.Product;
import com.vadastore.music_store_api.domain.Seller;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final SellerRepository sellerRepository;

    public List<ProductResponse> listAllAvailableProducts() {
        List<Product> products = productRepository.findByStockGreaterThan(0);

        if (products.isEmpty()) {
            throw new IllegalArgumentException("No products available in stock");
        }

       return products.stream().map(ProductResponse::fromEntity).toList();
    }

    public ProductResponse findProductById(Long id) {
        Product product = productRepository.findProductById(id).orElseThrow(() -> new EntityNotFoundException("Couldn't find product"));

        return ProductResponse.fromEntity(product);

    }

    public ProductResponse saveProduct(ProductRequest request, Long sellerId) {

        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new EntityNotFoundException("Seller not found"));

        Product productEntity = request.toEntity(seller);
        Product savedProduct = productRepository.save(productEntity);
        return ProductResponse.fromEntity(savedProduct);
    }

    public ProductResponse editProduct(Long id, ProductRequest updatedData) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        BigDecimal newPrice = updatedData.price() != null ? updatedData.price() : product.getPrice();
        BigDecimal newPromoPrice = updatedData.promotionalPrice() != null ? updatedData.promotionalPrice() : product.getPromotionalPrice();

        if (newPromoPrice != null && newPromoPrice.compareTo(newPrice) >= 0) {
            throw new IllegalArgumentException("Promotional price must be strictly lower than regular price");
        }
        if (updatedData.price() != null) product.setPrice(updatedData.price());

        if (updatedData.stockQuantity() != null) product.setStock(updatedData.stockQuantity());

        if (updatedData.name() != null) product.setName(updatedData.name());

        if (updatedData.promotionalPrice() != null) product.setPromotionalPrice(updatedData.promotionalPrice());

        if (updatedData.height() != null) product.setHeight(updatedData.height());

        if (updatedData.width() != null) product.setWidth(updatedData.width());

        if (updatedData.length() != null) product.setLength(updatedData.length());

        if (updatedData.description() != null) product.setDescription(updatedData.description());

        if (updatedData.weight() != null) product.setWeight(updatedData.weight());

        if (updatedData.tags() != null) product.setTags(updatedData.tags());

        if (updatedData.mainImageUrl() != null) product.setMainImageUrl(updatedData.mainImageUrl());

        if (updatedData.galleryImageUrls() != null) product.setGalleryImageUrls(updatedData.galleryImageUrls());

        if (updatedData.minStockThreshold() != null) product.setMinStockThreshold(updatedData.minStockThreshold());

        if (updatedData.active() != null) product.setIsActive(updatedData.active());

        productRepository.save(product);

        return   ProductResponse.fromEntity(product);
    }

    public ProductResponse decreaseStock(Long productId, int amount) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("Product out of stock or not found by this id"));
        int currentStock = product.getStock();

        if (product.getStock() < amount) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        int stockAfterPayment = currentStock - amount;
        product.setStock(stockAfterPayment);

        productRepository.save(product);

        return ProductResponse.fromEntity(product);
    }

    public void removeProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        productRepository.delete(product);
    }

    public ProductResponse applyDiscount(long id, double discountPercentage) {
        Product product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Couldn't find product"));
        if (discountPercentage >= 1 || discountPercentage < 0) {
            throw new IllegalArgumentException("Discount must be less than 100%");
        }

        BigDecimal finalPrice = product.getPrice().multiply(BigDecimal.valueOf(1 - discountPercentage));
        product.setPrice(finalPrice);

       return ProductResponse.fromEntity(productRepository.save(product));

    }

    public ProductResponse restockProduct(Long id, int amount) {
        Product productInDb = productRepository.findProductById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        int finalStock = productInDb.getStock() + amount;

        productInDb.setStock(finalStock);

        return ProductResponse.fromEntity(productRepository.save(productInDb));

    }

    public ProductResponse buyProduct(Long id, int amount) {
        Product product = productRepository.findProductById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (amount > product.getStock() || amount < 0) {
            throw new IllegalArgumentException("No stock available");
        }

        ProductResponse productStock = decreaseStock(id, amount);

        product.setStock(productStock.stockQuantity());

        return productStock;

    }

    public List<ProductResponse> findProductByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {

      if (minPrice == null || maxPrice == null) {
          throw new IllegalArgumentException("Prices cannot be null");
      }

       if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
           throw new IllegalArgumentException("Prices cannot be negative");
       }

       if (maxPrice.compareTo(minPrice) < 0) {
           throw new IllegalArgumentException("Max price must be greater than min price");
       }

     return productRepository.findByPriceBetween(minPrice, maxPrice).stream().map(ProductResponse::fromEntity).toList();
    }


}
