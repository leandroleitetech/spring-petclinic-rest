package org.springframework.samples.petclinic.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private static final int TOKEN_LENGTH = 32;
    private static final int RESET_TOKEN_VALIDITY_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is invalid");
        }

        validateNewPassword(currentPassword, newPassword, confirmPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public String requestPasswordReset(String username) {
        String token = generateToken();

        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setResetToken(token);
            user.setResetTokenExpiry(Instant.now().plus(RESET_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES));
            userRepository.save(user);
        }

        return token;
    }

    @Transactional
    public void confirmPasswordReset(String token, String newPassword, String confirmPassword) {
        User user = userRepository.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry() == null) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        if (Instant.now().isAfter(user.getResetTokenExpiry())) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        validateNewPassword(null, newPassword, confirmPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    private void validateNewPassword(String currentPassword, String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (currentPassword != null && currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
