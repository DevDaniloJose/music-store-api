package com.vadastore.music_store_api.record;

import com.vadastore.music_store_api.enums.Role;

public record UserDTO(Long id, String username, String email, java.util.Set<Role> roles) {
}
