package com.vadastore.music_store_api.record;


public record LoginResponse(String username, String email, String accessToken, String refreshToken, Long userId) {
}
