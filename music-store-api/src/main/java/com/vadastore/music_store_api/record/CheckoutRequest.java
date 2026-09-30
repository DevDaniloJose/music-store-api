package com.vadastore.music_store_api.record;

import com.vadastore.music_store_api.enums.PaymentMethod;
import com.vadastore.music_store_api.enums.ShippingMethod;
import com.vadastore.music_store_api.enums.CouponCode;

public record CheckoutRequest(PaymentMethod paymentMethod, ShippingMethod shippingMethod, String observation,
                              Long shippingAddressId) {
}
