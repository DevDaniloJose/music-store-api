package com.vadastore.music_store_api.repository;


import com.vadastore.music_store_api.domain.Order;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerId(Long id);


    Optional<Order> findByIdAndBuyerId(Long orderId, Long buyerId);


}
