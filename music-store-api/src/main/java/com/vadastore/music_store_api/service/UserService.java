package com.vadastore.music_store_api.service;


import com.vadastore.music_store_api.exceptions.UserAlreadyExistsException;
import com.vadastore.music_store_api.enums.Role;
import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.record.*;
import com.vadastore.music_store_api.repository.UserRepository;
import com.vadastore.music_store_api.security.JwtUtility;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {

   private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtility jwtUtility;
    private final AuthService authService;

    public SignUpResponse saveUser(SignUpRequest dto) throws UserAlreadyExistsException {

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .roles(Collections.singleton(Role.USER))
                .build();

        User savedUser = userRepository.save(user);


        String token = authService.generateTokenForUser(savedUser);

        return new SignUpResponse(savedUser.getEmail(), user.getId(), token);
    }

    public SignUpResponse saveAdmin(SignUpRequest dto) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = User.builder()
                .username(dto.username())
                .email(dto.username()).
                password(passwordEncoder.encode(dto.password()))
                .roles(Collections.singleton(Role.ADMIN)).
                build();


        User savedUser = userRepository.save(user);
        String token = authService.generateTokenForUser(savedUser);

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

