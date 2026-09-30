package com.vadastore.music_store_api.controller;

import com.vadastore.music_store_api.domain.ResetToken;
import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.record.ResetPasswordRequest;
import com.vadastore.music_store_api.record.ResetRequest;
import com.vadastore.music_store_api.repository.ResetTokenRepository;
import com.vadastore.music_store_api.repository.UserRepository;
import com.vadastore.music_store_api.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PasswordResetController {

    private final ResetTokenRepository resetTokenRepository;


    private final PasswordResetService resetService;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    @PostMapping("/reset-password")
    public ResponseEntity<String> requestReset(@Valid @RequestBody ResetRequest request) {
        resetService.processRequest(request.email());
        return ResponseEntity.ok("if the email is registered, you'll get a reset link");
    }


    @GetMapping("/reset-password")
    public ResponseEntity<String> validateToken(@RequestParam("token") String token) {
        Optional<ResetToken> tokenOpt = resetTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        }

        return ResponseEntity.ok("token is valid");
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        Optional<ResetToken> tokenOpt = resetTokenRepository.findByToken(request.getToken());

        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("token is invalid");
        }


        ResetToken tokenRecord = tokenOpt.get();
        User user = tokenRecord.getUser();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        resetTokenRepository.delete(tokenRecord);

        return ResponseEntity.ok("password updated");

    }





}
