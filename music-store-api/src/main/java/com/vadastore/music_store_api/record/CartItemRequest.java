package com.vadastore.music_store_api.record;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(

       @NotNull(message = "product id must have an id")
        Long productId,

       @Min(value = 1, message = "Stock cannot be negative or lesser than 1")
        int quantity) {
}
