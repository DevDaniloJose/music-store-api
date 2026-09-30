package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.domain.Buyer;
import com.vadastore.music_store_api.exceptions.ResourceNotFoundException;
import com.vadastore.music_store_api.repository.BuyerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuyerService {

    private final BuyerRepository buyerRepository;

    public Buyer findBuyerByUserId(Long userId) {
        Buyer buyer = buyerRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("user not found"));

        if (buyer.getCart() == null) {

            throw new ResourceNotFoundException("couldn't find cart");
        }

        return buyer;

    }

}
