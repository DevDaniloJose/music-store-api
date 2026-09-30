package com.vadastore.music_store_api.repository;

import com.vadastore.music_store_api.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByIsActive(boolean isActive);

}
