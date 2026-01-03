package org.spacelab.housingutilitiessystemchairman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.Admin;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private AdminService adminService;

    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = new Admin();
        testAdmin.setId("admin-id");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setPassword("encodedPassword");
        testAdmin.setEnabled(true);
    }

    @Test
    @DisplayName("Should create admin successfully")
    void createAdmin_shouldCreateAdmin() {
        String email = "admin@test.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        Admin result = adminService.createAdmin(email, password);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(passwordEncoder).encode(password);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Should find admin by email when exists")
    void findByEmail_shouldReturnAdmin_whenExists() {
        when(adminRepository.findByEmail("admin@test.com")).thenReturn(testAdmin);

        Optional<Admin> result = adminService.findByEmail("admin@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("Should return empty when admin not found by email")
    void findByEmail_shouldReturnEmpty_whenNotFound() {
        when(adminRepository.findByEmail(anyString())).thenReturn(null);

        Optional<Admin> result = adminService.findByEmail("unknown@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find admin by id when exists")
    void findById_shouldReturnAdmin_whenExists() {
        when(adminRepository.findById("admin-id")).thenReturn(Optional.of(testAdmin));

        Optional<Admin> result = adminService.findById("admin-id");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("admin-id");
    }

    @Test
    @DisplayName("Should return empty when admin not found by id")
    void findById_shouldReturnEmpty_whenNotFound() {
        when(adminRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<Admin> result = adminService.findById("unknown-id");

        assertThat(result).isEmpty();
    }
}
