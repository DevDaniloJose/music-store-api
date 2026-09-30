package com.vadastore.music_store_api.repository;

import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.domain.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    Optional<Buyer> findBuyerById(Long buyerId);
    Optional<Buyer> findByUserId(Long userId);
}
