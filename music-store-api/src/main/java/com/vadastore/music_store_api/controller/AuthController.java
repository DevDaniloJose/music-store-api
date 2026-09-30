package com.vadastore.music_store_api.controller;

import com.vadastore.music_store_api.domain.RefreshToken;
import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.exceptions.TokenRefreshException;
import com.vadastore.music_store_api.record.*;
import com.vadastore.music_store_api.repository.RefreshTokenRepository;
import com.vadastore.music_store_api.security.JwtUtility;
import com.vadastore.music_store_api.service.AuthService;
import com.vadastore.music_store_api.service.BuyerService;
import com.vadastore.music_store_api.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


        private final RefreshTokenRepository refreshTokenRepository;
        private final RefreshTokenService refreshTokenService;
        private final JwtUtility jwtUtility;
        private final AuthService authService;
        private final BuyerService buyerService;
          @Value("${jwt.expiration}")
          private Long jwtExpiration;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody Map<String, String> payload) {
        String requestToken = payload.get("refreshToken");

        RefreshToken token = refreshTokenRepository.findByToken(requestToken).orElseThrow(() -> new TokenRefreshException("invalid refresh token"));

        if (refreshTokenService.isExpired(token)) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token expired. Please log in again");
        }

        User user = token.getUser();
        Map<String, String> extraClaims = authService.claimsFilled(user);
        String newJwt = jwtUtility.generateToken(extraClaims, token.getUser().getEmail(), jwtExpiration);
        return ResponseEntity.ok(new TokenResponse(newJwt));

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request)  {
       return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }


    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid  @RequestBody SignUpRequest request) {
        return new ResponseEntity<>(authService.signUp(request), HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(@Valid @RequestBody LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken()).orElseThrow(() -> new TokenRefreshException("invalid refreshToken"));
        refreshTokenRepository.delete(refreshToken);

        return ResponseEntity.noContent().build();
    }


}
