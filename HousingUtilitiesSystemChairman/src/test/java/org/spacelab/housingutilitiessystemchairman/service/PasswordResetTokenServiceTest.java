package org.spacelab.housingutilitiessystemchairman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.PasswordResetToken;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.PasswordResetTokenRepository;
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
    private ChairmanRepository chairmanRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    private Chairman testChairman;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        testChairman = new Chairman();
        testChairman.setId("chairman-id");
        testChairman.setEmail("chairman@test.com");

        testToken = new PasswordResetToken("test-token", testChairman);
    }

    @Nested
    @DisplayName("Token Creation")
    class TokenCreation {
        @Test
        @DisplayName("Should create and save password reset token")
        void createAndSavePasswordResetToken_shouldCreate() {
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);
            when(passwordResetTokenRepository.findByToken(any())).thenReturn(Optional.of(testToken));

            String token = passwordResetTokenService.createAndSavePasswordResetToken(testChairman);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {
        @Test
        @DisplayName("Should return true for valid non-expired token")
        void validatePasswordResetToken_shouldReturnTrue() {
            PasswordResetToken validToken = mock(PasswordResetToken.class);
            when(validToken.getExpirationDate()).thenReturn(LocalDateTime.now().plusHours(1));
            when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

            boolean result = passwordResetTokenService.validatePasswordResetToken("valid-token");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for expired token")
        void validatePasswordResetToken_shouldReturnFalse_forExpiredToken() {
            PasswordResetToken expiredToken = mock(PasswordResetToken.class);
            when(expiredToken.getExpirationDate()).thenReturn(LocalDateTime.now().minusHours(1));
            when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            boolean result = passwordResetTokenService.validatePasswordResetToken("expired-token");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for non-existent token")
        void validatePasswordResetToken_shouldReturnFalse_forNonExistent() {
            when(passwordResetTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

            boolean result = passwordResetTokenService.validatePasswordResetToken("unknown-token");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Password Update")
    class PasswordUpdate {
        @Test
        @DisplayName("Should update password successfully")
        void updatePassword_shouldUpdatePassword() {
            when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));
            when(chairmanRepository.findById("chairman-id")).thenReturn(Optional.of(testChairman));
            when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);

            passwordResetTokenService.updatePassword("test-token", "newPassword");

            verify(passwordEncoder).encode("newPassword");
            verify(chairmanRepository).save(any(Chairman.class));
        }

        @Test
        @DisplayName("Should throw exception when chairman not found")
        void updatePassword_shouldThrowException_whenChairmanNotFound() {
            when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));
            when(chairmanRepository.findById("chairman-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetTokenService.updatePassword("test-token", "newPassword"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Chairman not found");
        }
    }
}
