package com.vadastore.music_store_api.util;

import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.enums.DocumentType;
import com.vadastore.music_store_api.enums.Role;
import com.vadastore.music_store_api.domain.Address;

import java.util.Collections;
import java.util.List;

public class UserCreator {

    public static User userCreator() {
        return User.builder()
                .id(1L)
                .username("Wyatt")
                .email("wyattshears@gmail.com")
                .documentType(DocumentType.CPF)
                .password("123")
                .roles(Collections.singleton(Role.USER))
                .build();
    }

}
