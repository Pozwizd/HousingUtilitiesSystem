package org.spacelab.housingutilitiessystemadmin.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.Admin;
import org.spacelab.housingutilitiessystemadmin.entity.PasswordResetToken;
import org.spacelab.housingutilitiessystemadmin.repository.AdminRepository;
import org.spacelab.housingutilitiessystemadmin.repository.PasswordResetTokenRepository;
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
    private final AdminRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updatePassword(String token, String password) {
        log.debug("updatePassword() - Starting password update for token: {}", token);
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(token).get();
        Admin admin = passwordResetToken.getAdminUser();
        log.debug("updatePassword() - Found admin with email: {}", admin.getEmail());
        
        // Получаем админа заново из репозитория, чтобы получить управляемую сущность
        String adminId = admin.getId();
        Admin managedAdmin = userRepository.findById(adminId).orElseThrow(
            () -> new RuntimeException("Admin not found with id: " + adminId)
        );
        
        managedAdmin.setPassword(passwordEncoder.encode(password));
        userRepository.save(managedAdmin);
        log.debug("updatePassword() - Password updated successfully for admin: {}", managedAdmin.getEmail());
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


    public String createAndSavePasswordResetToken(Admin admin) {
        log.debug("createAndSavePasswordResetToken() - Creating password reset token for admin: {}", admin.getEmail());
        String token = UUID.randomUUID().toString();
        
        // Создаем новый токен
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, admin);
        log.debug("createAndSavePasswordResetToken() - Creating new token: {} with expiration: {}", token, passwordResetToken.getExpirationDate());
        
        log.debug("createAndSavePasswordResetToken() - Saving token to repository...");
        PasswordResetToken savedToken = passwordResetTokenRepository.save(passwordResetToken);
        log.debug("createAndSavePasswordResetToken() - Saved token to DB - ID: {}, token: {}, expiration after save: {}", 
            savedToken.getId(), savedToken.getToken(), savedToken.getExpirationDate());
        
        // Проверяем, что токен действительно сохранился
        passwordResetTokenRepository.findByToken(token).ifPresentOrElse(
            found -> log.debug("createAndSavePasswordResetToken() - Verification: Token found in DB with ID: {}, token field: {}", found.getId(), found.getToken()),
            () -> log.error("createAndSavePasswordResetToken() - ERROR: Token NOT found in DB after save!")
        );
        
        log.info("createAndSavePasswordResetToken() - Password reset token created successfully for admin: {}, token: {}, expires at: {}", 
            admin.getEmail(), token, savedToken.getExpirationDate());
        return token;
    }
}
