package com.vadastore.music_store_api.service;

import com.vadastore.music_store_api.domain.ResetToken;
import com.vadastore.music_store_api.domain.User;
import com.vadastore.music_store_api.repository.ResetTokenRepository;
import com.vadastore.music_store_api.repository.UserRepository;
import com.vadastore.music_store_api.util.TokenGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final TokenGenerator tokenGenerator;
    private final UserRepository userRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final MailSender mailSender;

    public void processRequest(String email) {
         userRepository.findByEmail(email).ifPresent(user -> {
            String token = tokenGenerator.makeResetToken();

            ResetToken record = new ResetToken();
            record.setToken(token);
            record.setUser(user);
            record.setExpiresAt(LocalDateTime.now().plusMinutes(30));

            resetTokenRepository.save(record);

            sendEmail(user.getEmail(), token);
        });

    }


    private void sendEmail(String email, String token) {
        String resetUrl = "https://localhost:8080/reset-password?token=" + token;
        String body = "Click the link below to reset your password.\n" + resetUrl;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password reject");
        message.setText(body);

        mailSender.send(message);
    }
}
