package com.vadastore.music_store_api.util;
import com.vadastore.music_store_api.domain.Buyer;
import com.vadastore.music_store_api.domain.Cart;
import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.util.UserCreator;
import com.vadastore.music_store_api.domain.Address;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuyerCreator {


    public static Buyer buyerCreator() {
        User user = UserCreator.userCreator();



        return Buyer.builder()
                .cart( Cart.builder()
                        .id(1L).items(List.of()).build())
                .id(1L)
                .user(user)
                .address(Collections.singletonList(Address.builder()
                        .id(1L)
                        .buyer(null)
                        .street("flower street").city("sao paulo").state("SP")
                        .zipCode("33333-090")
                        .build()))
                .createdAt(LocalDateTime.now())
                .build();
    }

}
