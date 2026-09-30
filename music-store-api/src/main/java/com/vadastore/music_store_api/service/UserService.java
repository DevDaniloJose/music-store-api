package com.vadastore.music_store_api.service;


import com.vadastore.music_store_api.domain.Buyer;
import com.vadastore.music_store_api.domain.Cart;
import com.vadastore.music_store_api.exceptions.UserAlreadyExistsException;
import com.vadastore.music_store_api.enums.Role;
import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.record.*;
import com.vadastore.music_store_api.repository.BuyerRepository;
import com.vadastore.music_store_api.repository.UserRepository;
import com.vadastore.music_store_api.security.JwtUtility;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

   private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtility jwtUtility;
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    private final BuyerRepository buyerRepository;

    @Transactional
    public SignUpResponse registerNewBuyer(SignUpRequest dto) throws UserAlreadyExistsException {

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .roles(Collections.singleton(Role.BUYER))
                .build();

        User savedUser = userRepository.save(user);

        Cart cart = new Cart();

        Buyer buyer = Buyer.builder().user(user).cart(cart).build();

        cart.setBuyer(buyer);

        buyerRepository.save(buyer);

        String token = jwtUtility.generateToken(Map.of(), savedUser.getEmail(), jwtExpiration);

        return new SignUpResponse(savedUser.getEmail(), user.getId(), token);
    }

    public SignUpResponse saveAdmin(SignUpRequest dto) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = User.builder()
                .username(dto.username())
                .email(dto.email()).
                password(passwordEncoder.encode(dto.password()))
                .roles(Collections.singleton(Role.ADMIN)).
                build();


        User savedUser = userRepository.save(user);
        String token = jwtUtility.generateToken(Map.of(), savedUser.getEmail(), jwtExpiration);

        return new SignUpResponse(savedUser.getEmail(), savedUser.getId(), token);
    }


    public UserDTO findProfileByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );
    }

    }

