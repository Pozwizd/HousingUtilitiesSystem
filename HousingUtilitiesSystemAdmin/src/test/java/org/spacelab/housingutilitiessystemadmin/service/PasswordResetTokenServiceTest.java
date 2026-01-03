package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.Admin;
import org.spacelab.housingutilitiessystemadmin.entity.PasswordResetToken;
import org.spacelab.housingutilitiessystemadmin.repository.AdminRepository;
import org.spacelab.housingutilitiessystemadmin.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetTokenService Tests")
class PasswordResetTokenServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    private Admin testAdmin;
    private PasswordResetToken testToken;
    private String tokenString;

    @BeforeEach
    void setUp() {
        tokenString = "test-token-123";
        
        testAdmin = new Admin();
        testAdmin.setId("admin-id-123");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setPassword("oldEncodedPassword");

        testToken = new PasswordResetToken(tokenString, testAdmin);
    }

    @Nested
    @DisplayName("updatePassword Tests")
    class UpdatePasswordTests {

        @Test
        @DisplayName("Should update password successfully")
        void updatePassword_shouldUpdatePassword() {
            // Given
            String newPassword = "newPassword123";
            String encodedPassword = "encodedNewPassword";

            when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(testToken));
            when(adminRepository.findById("admin-id-123")).thenReturn(Optional.of(testAdmin));
            when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            // When
            passwordResetTokenService.updatePassword(tokenString, newPassword);

            // Then
            verify(passwordEncoder).encode(newPassword);
            verify(adminRepository).save(testAdmin);
            assertThat(testAdmin.getPassword()).isEqualTo(encodedPassword);
        }

        @Test
        @DisplayName("Should throw exception when admin not found")
        void updatePassword_shouldThrowException_whenAdminNotFound() {
            // Given
            when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(testToken));
            when(adminRepository.findById("admin-id-123")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> passwordResetTokenService.updatePassword(tokenString, "newPassword"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Admin not found");
        }
    }

    @Nested
    @DisplayName("validatePasswordResetToken Tests")
    class ValidatePasswordResetTokenTests {

        @Test
        @DisplayName("Should return true for valid non-expired token")
        void validatePasswordResetToken_shouldReturnTrue_forValidToken() {
            // Given
            testToken = new PasswordResetToken(tokenString, testAdmin);
            // Default constructor sets expiration to 24 hours from now
            when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(testToken));

            // When
            boolean result = passwordResetTokenService.validatePasswordResetToken(tokenString);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when token not found")
        void validatePasswordResetToken_shouldReturnFalse_whenTokenNotFound() {
            // Given
            when(passwordResetTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

            // When
            boolean result = passwordResetTokenService.validatePasswordResetToken("nonexistent-token");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for expired token")
        void validatePasswordResetToken_shouldReturnFalse_forExpiredToken() {
            // Given
            PasswordResetToken expiredToken = mock(PasswordResetToken.class);
            when(expiredToken.getExpirationDate()).thenReturn(LocalDateTime.now().minusHours(1));
            when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(expiredToken));

            // When
            boolean result = passwordResetTokenService.validatePasswordResetToken(tokenString);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("createAndSavePasswordResetToken Tests")
    class CreateAndSavePasswordResetTokenTests {

        @Test
        @DisplayName("Should create and save token")
        void createAndSavePasswordResetToken_shouldCreateAndSaveToken() {
            // Given
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> {
                PasswordResetToken token = invocation.getArgument(0);
                token.setId("token-id-123");
                return token;
            });
            when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(testToken));

            // When
            String result = passwordResetTokenService.createAndSavePasswordResetToken(testAdmin);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("Should generate unique UUID token")
        void createAndSavePasswordResetToken_shouldGenerateUniqueToken() {
            // Given
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(testToken));

            // When
            String token1 = passwordResetTokenService.createAndSavePasswordResetToken(testAdmin);
            String token2 = passwordResetTokenService.createAndSavePasswordResetToken(testAdmin);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }
}
