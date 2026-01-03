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
import org.spacelab.housingutilitiessystemadmin.exception.OperationException;
import org.spacelab.housingutilitiessystemadmin.models.admin.ProfileUpdateRequest;
import org.spacelab.housingutilitiessystemadmin.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Tests")
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FileService fileService;

    @InjectMocks
    private AdminService adminService;

    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = new Admin();
        testAdmin.setId("admin-id-123");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setPassword("encodedPassword");
        testAdmin.setUsername("TestAdmin");
        testAdmin.setEnabled(true);
    }

    @Nested
    @DisplayName("createAdmin Tests")
    class CreateAdminTests {

        @Test
        @DisplayName("Should create admin with email and password")
        void createAdmin_shouldCreateAndSaveAdmin() {
            // Given
            String email = "new@admin.com";
            String password = "password123";
            String encodedPassword = "encodedPassword";

            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
            when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> {
                Admin saved = invocation.getArgument(0);
                saved.setId("new-admin-id");
                return saved;
            });

            // When
            Admin result = adminService.createAdmin(email, password);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.isEnabled()).isTrue();

            verify(passwordEncoder).encode(password);
            verify(adminRepository).save(any(Admin.class));
        }

        @Test
        @DisplayName("Should create admin with email, password and username")
        void createAdmin_withUsername_shouldCreateAndSaveAdmin() {
            // Given
            String email = "new@admin.com";
            String password = "password123";
            String username = "NewAdmin";
            String encodedPassword = "encodedPassword";

            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
            when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Admin result = adminService.createAdmin(email, password, username);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getDisplayName()).isEqualTo(username);
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.isEnabled()).isTrue();

            verify(passwordEncoder).encode(password);
            verify(adminRepository).save(any(Admin.class));
        }
    }

    @Nested
    @DisplayName("findByEmail Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should return admin when found")
        void findByEmail_shouldReturnAdmin() {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);

            // When
            Admin result = adminService.findByEmail("admin@test.com");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("admin@test.com");
        }

        @Test
        @DisplayName("Should return null when admin not found")
        void findByEmail_shouldReturnNull_whenNotFound() {
            // Given
            when(adminRepository.findByEmail("notfound@test.com")).thenReturn(null);

            // When
            Admin result = adminService.findByEmail("notfound@test.com");

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("findOptByEmail Tests")
    class FindOptByEmailTests {

        @Test
        @DisplayName("Should return Optional with admin when found")
        void findOptByEmail_shouldReturnOptionalWithAdmin() {
            // Given
            when(adminRepository.findOptByEmail("admin@test.com")).thenReturn(Optional.of(testAdmin));

            // When
            Optional<Admin> result = adminService.findOptByEmail("admin@test.com");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("admin@test.com");
        }

        @Test
        @DisplayName("Should return empty Optional when not found")
        void findOptByEmail_shouldReturnEmptyOptional_whenNotFound() {
            // Given
            when(adminRepository.findOptByEmail("notfound@test.com")).thenReturn(Optional.empty());

            // When
            Optional<Admin> result = adminService.findOptByEmail("notfound@test.com");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return Optional with admin when found")
        void findById_shouldReturnOptionalWithAdmin() {
            // Given
            when(adminRepository.findById("admin-id-123")).thenReturn(Optional.of(testAdmin));

            // When
            Optional<Admin> result = adminService.findById("admin-id-123");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("admin-id-123");
        }

        @Test
        @DisplayName("Should return empty Optional when not found")
        void findById_shouldReturnEmptyOptional_whenNotFound() {
            // Given
            when(adminRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

            // When
            Optional<Admin> result = adminService.findById("nonexistent-id");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("checkPassword Tests")
    class CheckPasswordTests {

        @Test
        @DisplayName("Should return true when passwords match")
        void checkPassword_shouldReturnTrue_whenPasswordsMatch() {
            // Given
            when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

            // When
            boolean result = adminService.checkPassword("rawPassword", "encodedPassword");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when passwords don't match")
        void checkPassword_shouldReturnFalse_whenPasswordsDontMatch() {
            // Given
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            // When
            boolean result = adminService.checkPassword("wrongPassword", "encodedPassword");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should throw exception when admin not found")
        void updateProfile_shouldThrowException_whenAdminNotFound() {
            // Given
            when(adminRepository.findByEmail("notfound@test.com")).thenReturn(null);
            ProfileUpdateRequest request = new ProfileUpdateRequest();

            // When/Then
            assertThatThrownBy(() -> adminService.updateProfile("notfound@test.com", request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }

        @Test
        @DisplayName("Should update username when provided")
        void updateProfile_shouldUpdateUsername_whenProvided() {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setUsername("NewUsername");

            // When
            Admin result = adminService.updateProfile("admin@test.com", request);

            // Then
            assertThat(result.getDisplayName()).isEqualTo("NewUsername");
            verify(adminRepository).save(any(Admin.class));
        }

        @Test
        @DisplayName("Should not update username when null or empty")
        void updateProfile_shouldNotUpdateUsername_whenNullOrEmpty() {
            // Given
            testAdmin.setUsername("OriginalUsername");
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setUsername("");

            // When
            Admin result = adminService.updateProfile("admin@test.com", request);

            // Then
            assertThat(result.getDisplayName()).isEqualTo("OriginalUsername");
        }

        @Test
        @DisplayName("Should throw exception when changing password without current password")
        void updateProfile_shouldThrowException_whenChangingPasswordWithoutCurrentPassword() {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setPassword("newPassword");
            request.setCurrentPassword(null);

            // When/Then
            assertThatThrownBy(() -> adminService.updateProfile("admin@test.com", request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Необходимо указать текущий пароль");
        }

        @Test
        @DisplayName("Should throw exception when changing password with empty current password")
        void updateProfile_shouldThrowException_whenChangingPasswordWithEmptyCurrentPassword() {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setPassword("newPassword");
            request.setCurrentPassword("");

            // When/Then
            assertThatThrownBy(() -> adminService.updateProfile("admin@test.com", request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Необходимо указать текущий пароль");
        }

        @Test
        @DisplayName("Should throw exception when current password is wrong")
        void updateProfile_shouldThrowException_whenCurrentPasswordIsWrong() {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(passwordEncoder.matches("wrongCurrentPassword", "encodedPassword")).thenReturn(false);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setPassword("newPassword");
            request.setCurrentPassword("wrongCurrentPassword");

            // When/Then
            assertThatThrownBy(() -> adminService.updateProfile("admin@test.com", request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Неверный текущий пароль");
        }

        @Test
        @DisplayName("Should update password when correct current password provided")
        void updateProfile_shouldUpdatePassword_whenCorrectCurrentPasswordProvided() {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(passwordEncoder.matches("correctCurrentPassword", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setPassword("newPassword");
            request.setCurrentPassword("correctCurrentPassword");

            // When
            adminService.updateProfile("admin@test.com", request);

            // Then
            verify(passwordEncoder).encode("newPassword");
            verify(adminRepository).save(any(Admin.class));
        }

        @Test
        @DisplayName("Should throw exception when email is already taken by another admin")
        void updateProfile_shouldThrowException_whenEmailAlreadyTaken() {
            // Given
            Admin anotherAdmin = new Admin();
            anotherAdmin.setId("another-admin-id");
            anotherAdmin.setEmail("newemail@test.com");

            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(adminRepository.findByEmail("newemail@test.com")).thenReturn(anotherAdmin);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setEmail("newemail@test.com");

            // When/Then
            assertThatThrownBy(() -> adminService.updateProfile("admin@test.com", request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("уже используется");
        }

        @Test
        @DisplayName("Should update email when not taken")
        void updateProfile_shouldUpdateEmail_whenNotTaken() {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(adminRepository.findByEmail("newemail@test.com")).thenReturn(null);
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setEmail("newemail@test.com");

            // When
            Admin result = adminService.updateProfile("admin@test.com", request);

            // Then
            assertThat(result.getEmail()).isEqualTo("newemail@test.com");
        }

        @Test
        @DisplayName("Should allow updating to same email (no change)")
        void updateProfile_shouldAllowSameEmail() {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setEmail("admin@test.com"); // Same email

            // When
            Admin result = adminService.updateProfile("admin@test.com", request);

            // Then
            assertThat(result.getEmail()).isEqualTo("admin@test.com");
            verify(adminRepository, times(1)).findByEmail("admin@test.com");
        }

        @Test
        @DisplayName("Should update all profile fields")
        void updateProfile_shouldUpdateAllProfileFields() {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setPhone("+1234567890");
            request.setOrganization("Test Organization");
            request.setAddress("123 Test Street");
            request.setCity("Test City");
            request.setState("Test State");
            request.setZipCode("12345");
            request.setCountry("Test Country");
            request.setLanguage("en");
            request.setTimezone("UTC");
            request.setCurrency("USD");

            // When
            Admin result = adminService.updateProfile("admin@test.com", request);

            // Then
            assertThat(result.getPhone()).isEqualTo("+1234567890");
            assertThat(result.getOrganization()).isEqualTo("Test Organization");
            assertThat(result.getAddress()).isEqualTo("123 Test Street");
            assertThat(result.getCity()).isEqualTo("Test City");
            assertThat(result.getState()).isEqualTo("Test State");
            assertThat(result.getZipCode()).isEqualTo("12345");
            assertThat(result.getCountry()).isEqualTo("Test Country");
            assertThat(result.getLanguage()).isEqualTo("en");
            assertThat(result.getTimezone()).isEqualTo("UTC");
            assertThat(result.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should not update fields when null")
        void updateProfile_shouldNotUpdateFields_whenNull() {
            // Given
            testAdmin.setPhone("existingPhone");
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            // All fields are null

            // When
            Admin result = adminService.updateProfile("admin@test.com", request);

            // Then
            assertThat(result.getPhone()).isEqualTo("existingPhone");
        }
    }

    @Nested
    @DisplayName("updateAvatar Tests")
    class UpdateAvatarTests {

        @Mock
        private MultipartFile avatarFile;

        @Test
        @DisplayName("Should throw exception when admin not found")
        void updateAvatar_shouldThrowException_whenAdminNotFound() {
            // Given
            when(adminRepository.findByEmail("notfound@test.com")).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> adminService.updateAvatar("notfound@test.com", avatarFile))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }

        @Test
        @DisplayName("Should upload new avatar and save")
        void updateAvatar_shouldUploadAndSave() throws IOException {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(fileService.uploadFile(avatarFile)).thenReturn("uploads/new-avatar.jpg");
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            // When
            Admin result = adminService.updateAvatar("admin@test.com", avatarFile);

            // Then
            assertThat(result.getPathAvatar()).isEqualTo("uploads/new-avatar.jpg");
            verify(fileService).uploadFile(avatarFile);
            verify(adminRepository).save(any(Admin.class));
        }

        @Test
        @DisplayName("Should delete old avatar before uploading new one")
        void updateAvatar_shouldDeleteOldAvatarBeforeUploadingNew() throws IOException {
            // Given
            testAdmin.setPathAvatar("uploads/old-avatar.jpg");
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(fileService.uploadFile(avatarFile)).thenReturn("uploads/new-avatar.jpg");
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            // When
            adminService.updateAvatar("admin@test.com", avatarFile);

            // Then
            verify(fileService).deleteFile("uploads/old-avatar.jpg");
            verify(fileService).uploadFile(avatarFile);
        }

        @Test
        @DisplayName("Should not delete avatar when old path is null")
        void updateAvatar_shouldNotDeleteAvatar_whenOldPathIsNull() throws IOException {
            // Given
            testAdmin.setPathAvatar(null);
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(fileService.uploadFile(avatarFile)).thenReturn("uploads/new-avatar.jpg");
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            // When
            adminService.updateAvatar("admin@test.com", avatarFile);

            // Then
            verify(fileService, never()).deleteFile(anyString());
            verify(fileService).uploadFile(avatarFile);
        }

        @Test
        @DisplayName("Should not delete avatar when old path is empty")
        void updateAvatar_shouldNotDeleteAvatar_whenOldPathIsEmpty() throws IOException {
            // Given
            testAdmin.setPathAvatar("");
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(fileService.uploadFile(avatarFile)).thenReturn("uploads/new-avatar.jpg");
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            // When
            adminService.updateAvatar("admin@test.com", avatarFile);

            // Then
            verify(fileService, never()).deleteFile(anyString());
        }

        @Test
        @DisplayName("Should throw exception when file upload fails")
        void updateAvatar_shouldThrowException_whenUploadFails() throws IOException {
            // Given
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(fileService.uploadFile(avatarFile)).thenThrow(new IOException("Upload failed"));

            // When/Then
            assertThatThrownBy(() -> adminService.updateAvatar("admin@test.com", avatarFile))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Ошибка при загрузке файла");
        }
    }

    @Nested
    @DisplayName("deleteAvatar Tests")
    class DeleteAvatarTests {

        @Test
        @DisplayName("Should throw exception when admin not found")
        void deleteAvatar_shouldThrowException_whenAdminNotFound() {
            // Given
            when(adminRepository.findByEmail("notfound@test.com")).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> adminService.deleteAvatar("notfound@test.com"))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }

        @Test
        @DisplayName("Should delete avatar and return true")
        void deleteAvatar_shouldDeleteAndReturnTrue() throws IOException {
            // Given
            testAdmin.setPathAvatar("uploads/avatar.jpg");
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

            // When
            boolean result = adminService.deleteAvatar("admin@test.com");

            // Then
            assertThat(result).isTrue();
            assertThat(testAdmin.getPathAvatar()).isNull();
            verify(fileService).deleteFile("uploads/avatar.jpg");
            verify(adminRepository).save(any(Admin.class));
        }

        @Test
        @DisplayName("Should return false when no avatar to delete (null)")
        void deleteAvatar_shouldReturnFalse_whenNoAvatarToDelete_null() throws IOException {
            // Given
            testAdmin.setPathAvatar(null);
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);

            // When
            boolean result = adminService.deleteAvatar("admin@test.com");

            // Then
            assertThat(result).isFalse();
            verify(fileService, never()).deleteFile(anyString());
            verify(adminRepository, never()).save(any(Admin.class));
        }

        @Test
        @DisplayName("Should return false when no avatar to delete (empty)")
        void deleteAvatar_shouldReturnFalse_whenNoAvatarToDelete_empty() throws IOException {
            // Given
            testAdmin.setPathAvatar("");
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);

            // When
            boolean result = adminService.deleteAvatar("admin@test.com");

            // Then
            assertThat(result).isFalse();
            verify(fileService, never()).deleteFile(anyString());
        }

        @Test
        @DisplayName("Should throw exception when file deletion fails")
        void deleteAvatar_shouldThrowException_whenDeletionFails() throws IOException {
            // Given
            testAdmin.setPathAvatar("uploads/avatar.jpg");
            when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);
            doThrow(new IOException("Delete failed")).when(fileService).deleteFile("uploads/avatar.jpg");

            // When/Then
            assertThatThrownBy(() -> adminService.deleteAvatar("admin@test.com"))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Ошибка при удалении файла");
        }
    }
}
