package org.spacelab.housingutilitiessystemuser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemuser.entity.PasswordResetToken;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.repository.PasswordResetTokenRepository;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetTokenService Tests")
class PasswordResetTokenServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-id");
        testUser.setEmail("test@test.com");
        testUser.setPassword("old-password");

        testToken = new PasswordResetToken();
        testToken.setId("token-id");
        testToken.setToken("test-token");
        testToken.setUser(testUser);
        testToken.setExpirationDate(LocalDateTime.now().plusHours(1));
    }

    @Nested
    @DisplayName("Update Password")
    class UpdatePassword {
        @Test
        @DisplayName("Should update password successfully")
        void updatePassword_shouldUpdate() {
            when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            passwordResetTokenService.updatePassword("test-token", "new-password");

            verify(passwordEncoder).encode("new-password");
            verify(userRepository).save(testUser);
            assertThat(testUser.getPassword()).isEqualTo("encoded-new-password");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void updatePassword_shouldThrowWhenUserNotFound() {
            when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));
            when(userRepository.findById("user-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetTokenService.updatePassword("test-token", "new-password"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Validate Token")
    class ValidateToken {
        @Test
        @DisplayName("Should return true for valid token")
        void validatePasswordResetToken_shouldReturnTrue() {
            testToken.setExpirationDate(LocalDateTime.now().plusHours(1));
            when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));

            boolean result = passwordResetTokenService.validatePasswordResetToken("test-token");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for expired token")
        void validatePasswordResetToken_shouldReturnFalseForExpired() {
            testToken.setExpirationDate(LocalDateTime.now().minusHours(1));
            when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));

            boolean result = passwordResetTokenService.validatePasswordResetToken("test-token");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when token not found")
        void validatePasswordResetToken_shouldReturnFalseWhenNotFound() {
            when(passwordResetTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

            boolean result = passwordResetTokenService.validatePasswordResetToken("unknown-token");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true for token expiring in future")
        void validatePasswordResetToken_shouldReturnTrueForFutureExpiration() {
            testToken.setExpirationDate(LocalDateTime.now().plusMinutes(30));
            when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));

            boolean result = passwordResetTokenService.validatePasswordResetToken("test-token");

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Create Token")
    class CreateToken {
        @Test
        @DisplayName("Should create and save token")
        void createAndSavePasswordResetToken_shouldCreate() {
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);
            when(passwordResetTokenRepository.findByToken(any())).thenReturn(Optional.of(testToken));

            String result = passwordResetTokenService.createAndSavePasswordResetToken(testUser);

            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("Should handle verification when token not found after save")
        void createAndSavePasswordResetToken_shouldHandleVerificationNotFound() {
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);
            when(passwordResetTokenRepository.findByToken(any())).thenReturn(Optional.empty());

            String result = passwordResetTokenService.createAndSavePasswordResetToken(testUser);

            assertThat(result).isNotNull();
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }
    }
}
