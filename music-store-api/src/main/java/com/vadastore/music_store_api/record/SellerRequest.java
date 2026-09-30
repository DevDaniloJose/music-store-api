package com.vadastore.music_store_api.record;

import com.vadastore.music_store_api.enums.DocumentType;
import com.vadastore.music_store_api.domain.Seller;
import com.vadastore.music_store_api.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record SellerRequest(

                            @NotNull(message = "Store name is required")
                            String storeName,
                            @NotNull(message = "Document type is required")

                            DocumentType documentType,
                            @NotBlank(message = "Document number is required")
                            @Pattern(regexp = "^(\\d{11}|\\d{14})$",
                            message = "Document number must have exactly 11 digits (CPF) OR 14 digits (CNPJ)")

                            @NotNull(message = "Document Number is required")
                            String documentNumber,
                            @NotNull(message = "Postal code is required")
                            String postalCode,
                            List<ProductResponse> products)

{
            public Seller dtoToEntity(User user) {
                return Seller.builder()
                        .user(user)
                        .storeName(this.storeName)
                        .documentType(this.documentType)
                        .documentNumber(this.documentNumber)
                        .postalCode(this.postalCode).build();
            }
}
