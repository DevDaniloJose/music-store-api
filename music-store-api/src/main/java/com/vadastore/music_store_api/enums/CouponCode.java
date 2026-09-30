package com.vadastore.music_store_api.enums;

import java.math.BigDecimal;

public enum CouponCode {

    VADA10(new BigDecimal("0.90")),
    VADA15(new BigDecimal("0.85")),
    VADA20(new BigDecimal("0.80")),
    VADAVADA(new BigDecimal("0.75"));

    private final BigDecimal discountFactor;

    CouponCode(BigDecimal discountFactor) {
        this.discountFactor = discountFactor;
    }

    public BigDecimal getDiscountFactor() {
        return discountFactor;
    }
}
