package com.vadastore.music_store_api.repository;

import com.vadastore.music_store_api.domain.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    Optional<ResetToken> findByToken(String token);

}
