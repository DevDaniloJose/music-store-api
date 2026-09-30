package com.vadastore.music_store_api.record;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(

        @NotNull(message = "product id cannot be null")
        Long productId,

        @Min(value = 1, message = "quantity must be equal or greater than 0")
        int quantity) {
}
