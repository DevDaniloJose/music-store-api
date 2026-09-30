package com.vadastore.music_store_api.repository;

import com.vadastore.music_store_api.domain.Cart;
import com.vadastore.music_store_api.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByProductNameContainingIgnoreCase(String Productname);

    Optional<CartItem> findByProductIdAndCartId(Long ProductId, Long cartId);

    void deleteAllByCartId(Long cartId);

}
