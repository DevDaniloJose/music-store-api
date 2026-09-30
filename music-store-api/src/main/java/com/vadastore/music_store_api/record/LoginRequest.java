package com.vadastore.music_store_api.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "field email cannot be blank")
        @Email
        String email,

        @NotBlank(message = "field password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,

        Long guestCartId) {
}
