package com.vadastore.music_store_api.util;

import com.vadastore.music_store_api.enums.DocumentType;
import com.vadastore.music_store_api.domain.Seller;

import java.time.LocalDateTime;

public class SellerCreator {

    public static Seller createSeller() {
        return Seller.builder()
                .id(1L)
                .user(UserCreator.userCreator())
                .storeName("LAIKASHOP")
                .documentType(DocumentType.CNPJ)
                .documentNumber("12345678910")
                .postalCode("22222-010")
                .rating(5.0).isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
