package com.vadastore.music_store_api.service;


import com.vadastore.music_store_api.domain.RefreshToken;
import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.repository.RefreshTokenRepository;
import com.vadastore.music_store_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    @Value("${jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId).orElse(null);

        if (refreshToken == null) {
           refreshToken = RefreshToken.builder().user(user).token(UUID.randomUUID().toString())
                    .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs)).build();
        } else {
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        }
        return refreshTokenRepository.save(refreshToken);

    }

    public Boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }

}
