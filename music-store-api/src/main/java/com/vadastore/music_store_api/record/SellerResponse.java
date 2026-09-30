package com.vadastore.music_store_api.record;
import com.vadastore.music_store_api.domain.Seller;

public record SellerResponse(Long id, String name) {

    public static SellerResponse fromEntity(Seller seller) {
        return new SellerResponse(seller.getId(), seller.getStoreName());
    }




}
