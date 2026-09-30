package com.vadastore.music_store_api.repository;

import com.vadastore.music_store_api.enums.DocumentType;
import com.vadastore.music_store_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByDocumentType(DocumentType type);
}
