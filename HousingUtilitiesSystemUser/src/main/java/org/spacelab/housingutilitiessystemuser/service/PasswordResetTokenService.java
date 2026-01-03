package org.spacelab.housingutilitiessystemuser.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.entity.PasswordResetToken;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.spacelab.housingutilitiessystemuser.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updatePassword(String token, String password) {
        log.debug("updatePassword() - Starting password update for token: {}", token);
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(token).get();
        User user = passwordResetToken.getUser();
        log.debug("updatePassword() - Found user with email: {}", user.getEmail());

        
        
        String userId = user.getId();
        User managedUser = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found with id: " + userId));

        managedUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(managedUser);
        log.debug("updatePassword() - Password updated successfully for user: {}", managedUser.getEmail());
    }

    public boolean validatePasswordResetToken(String token) {
        log.debug("validatePasswordResetToken() - Validating token: {}", token);
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken.isEmpty()) {
            log.warn("validatePasswordResetToken() - Token not found: {}", token);
            return false;
        }
        LocalDateTime expirationDate = passwordResetToken.get().getExpirationDate();
        LocalDateTime now = LocalDateTime.now();
        log.debug("validatePasswordResetToken() - Token validation - Expiration: {}, Now: {}", expirationDate, now);
        boolean isValid = !expirationDate.isBefore(now);
        log.debug("validatePasswordResetToken() - Token is valid: {}", isValid);
        if (!isValid) {
            log.warn("validatePasswordResetToken() - Token expired. Expiration was: {}, current time: {}",
                    expirationDate, now);
        }
        return isValid;
    }

    public String createAndSavePasswordResetToken(User user) {
        log.debug("createAndSavePasswordResetToken() - Creating password reset token for user: {}", user.getEmail());
        String token = UUID.randomUUID().toString();

        
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        log.debug("createAndSavePasswordResetToken() - Creating new token: {} with expiration: {}", token,
                passwordResetToken.getExpirationDate());

        log.debug("createAndSavePasswordResetToken() - Saving token to repository...");
        PasswordResetToken savedToken = passwordResetTokenRepository.save(passwordResetToken);
        log.debug(
                "createAndSavePasswordResetToken() - Saved token to DB - ID: {}, token: {}, expiration after save: {}",
                savedToken.getId(), savedToken.getToken(), savedToken.getExpirationDate());

        
        passwordResetTokenRepository.findByToken(token).ifPresentOrElse(
                found -> log.debug(
                        "createAndSavePasswordResetToken() - Verification: Token found in DB with ID: {}, token field: {}",
                        found.getId(), found.getToken()),
                () -> log.error("createAndSavePasswordResetToken() - ERROR: Token NOT found in DB after save!"));

        log.info(
                "createAndSavePasswordResetToken() - Password reset token created successfully for user: {}, token: {}, expires at: {}",
                user.getEmail(), token, savedToken.getExpirationDate());
        return token;
    }
}
