package com.vadastore.music_store_api.record;

public record StockWarningResponse(long cartItemId, int requestedQuantity, int availableStock, String message) {
}
