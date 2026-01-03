package org.spacelab.housingutilitiessystemchairman.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.PasswordResetToken;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.PasswordResetTokenRepository;
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
    private final ChairmanRepository chairmanRepository;
    private final PasswordEncoder passwordEncoder;
    public void updatePassword(String token, String password) {
        log.debug("updatePassword() - Starting password update for token: {}", token);
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(token).get();
        Chairman chairman = passwordResetToken.getChairmanUser();
        log.debug("updatePassword() - Found chairman with email: {}", chairman.getEmail());
        String chairmanId = chairman.getId();
        Chairman managedChairman = chairmanRepository.findById(chairmanId).orElseThrow(
            () -> new RuntimeException("Chairman not found with id: " + chairmanId)
        );
        managedChairman.setPassword(passwordEncoder.encode(password));
        chairmanRepository.save(managedChairman);
        log.debug("updatePassword() - Password updated successfully for chairman: {}", managedChairman.getEmail());
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
            log.warn("validatePasswordResetToken() - Token expired. Expiration was: {}, current time: {}", expirationDate, now);
        }
        return isValid;
    }
    public String createAndSavePasswordResetToken(Chairman chairman) {
        log.debug("createAndSavePasswordResetToken() - Creating password reset token for chairman: {}", chairman.getEmail());
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, chairman);
        log.debug("createAndSavePasswordResetToken() - Creating new token: {} with expiration: {}", token, passwordResetToken.getExpirationDate());
        log.debug("createAndSavePasswordResetToken() - Saving token to repository...");
        PasswordResetToken savedToken = passwordResetTokenRepository.save(passwordResetToken);
        log.debug("createAndSavePasswordResetToken() - Saved token to DB - ID: {}, token: {}, expiration after save: {}",
            savedToken.getId(), savedToken.getToken(), savedToken.getExpirationDate());
        passwordResetTokenRepository.findByToken(token).ifPresentOrElse(
            found -> log.debug("createAndSavePasswordResetToken() - Verification: Token found in DB with ID: {}, token field: {}", found.getId(), found.getToken()),
            () -> log.error("createAndSavePasswordResetToken() - ERROR: Token NOT found in DB after save!")
        );
        log.info("createAndSavePasswordResetToken() - Password reset token created successfully for chairman: {}, token: {}, expires at: {}",
            chairman.getEmail(), token, savedToken.getExpirationDate());
        return token;
    }
}
