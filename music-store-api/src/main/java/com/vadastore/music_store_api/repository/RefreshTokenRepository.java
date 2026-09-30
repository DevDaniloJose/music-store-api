package com.vadastore.music_store_api.repository;

import com.vadastore.music_store_api.domain.RefreshToken;
import com.vadastore.music_store_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserId(Long userId);

}
