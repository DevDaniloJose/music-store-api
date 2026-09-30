package com.vadastore.music_store_api.repository;

import com.vadastore.music_store_api.enums.Category;
import com.vadastore.music_store_api.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerId(long sellerID);

    List<Product> findByCategory(Category category);

    List<Product> findByStockGreaterThan(int stock);

    Optional<Product> findProductById(Long id);

    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> findBySellerIsActiveTrue();
}
