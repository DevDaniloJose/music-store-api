package com.vadastore.music_store_api.repository;

import com.vadastore.music_store_api.domain.Address;
import com.vadastore.music_store_api.domain.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findById(Long id);

}
