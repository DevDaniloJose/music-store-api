package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.exceptions.InvalidCredentialsException;
import com.vadastore.music_store_api.domain.Buyer;
import com.vadastore.music_store_api.domain.Cart;
import com.vadastore.music_store_api.enums.Role;
import com.vadastore.music_store_api.domain.RefreshToken;
import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.exceptions.ResourceNotFoundException;
import com.vadastore.music_store_api.record.LoginRequest;
import com.vadastore.music_store_api.record.LoginResponse;
import com.vadastore.music_store_api.record.SignUpRequest;
import com.vadastore.music_store_api.record.SignUpResponse;
import com.vadastore.music_store_api.repository.RefreshTokenRepository;
import com.vadastore.music_store_api.repository.UserRepository;
import com.vadastore.music_store_api.security.JwtUtility;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.vadastore.music_store_api.repository.CartRepository;
import com.vadastore.music_store_api.repository.BuyerRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private final RefreshTokenService refreshTokenService;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final BuyerRepository buyerRepository;
    public String generateTokenForUser(User user) {

        Map<String, String> extraClaims = claimsFilled(user);

        long expireInterval = 86400000L;

       return jwtUtility.generateToken(extraClaims, user.getEmail(), expireInterval);
    }

    public LoginResponse login(LoginRequest request) throws InvalidCredentialsException {
        User userFound = userRepository.findByEmail(request.email()).orElseThrow(() -> new InvalidCredentialsException("Invalid Credentials"));
        if (!passwordEncoder.matches(request.password(), (userFound.getPassword()))) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        Map<String, String> extraClaims = claimsFilled(userFound);

        long expireInterval = 15 * 60 * 1000L;

        String accessToken = jwtUtility.generateToken(extraClaims, userFound.getUsername(), expireInterval);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userFound.getId());


        Buyer buyer = buyerRepository.findByUserId(userFound.getId()).orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found"));

      if (buyer.getCart() == null) {

          Cart userNewCart = cartRepository.save(Cart.builder().buyer(buyer).build());
          buyer.setCart(userNewCart);
          buyerRepository.save(buyer);
      }

      if (request.guestCartId() != null) {
          cartService.mergeGuestCartWithUserCart(request.guestCartId(), buyer.getCart().getId());
      }


      userRepository.save(userFound);

        return new LoginResponse(userFound.getUsername(), userFound.getEmail(), accessToken, refreshToken.getToken(), userFound.getId());

    }

    public Map<String, String> claimsFilled(User user) {

        Map<String, String> extraClaims = new HashMap<>();

        extraClaims.put("id", String.valueOf(user.getId()));
        extraClaims.put("role", user.getRoles().stream().map(r -> "ROLE_" + r.name()).collect(Collectors.joining(",")));
        extraClaims.put("username", user.getUsername());

        return extraClaims;
    }

    public SignUpResponse signUp(SignUpRequest request) throws InvalidCredentialsException {



        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new InvalidCredentialsException("invalid credentials");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Collections.singleton(Role.USER)).build();





        User savedUser = userRepository.save(user);

        Buyer buyer = Buyer.builder().user(savedUser).createdAt(LocalDateTime.now()).build();

        Cart cart = Cart.builder().buyer(buyer).build();

        Cart userNewCart = cartRepository.save(cart);
        buyer.setCart(userNewCart);
        buyerRepository.save(buyer);

        if (request.guestCartId() != null) {
            cartService.mergeGuestCartWithUserCart(request.guestCartId(), userNewCart.getId());
        }

        String token = generateTokenForUser(savedUser);

        return new SignUpResponse(savedUser.getEmail(), savedUser.getId(), token);
    }






}