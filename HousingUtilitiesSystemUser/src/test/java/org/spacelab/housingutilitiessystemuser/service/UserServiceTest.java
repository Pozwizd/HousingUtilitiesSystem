package org.spacelab.housingutilitiessystemuser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-id");
        testUser.setEmail("test@test.com");
        testUser.setLogin("test@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encoded-password");
        testUser.setEnabled(true);
    }

    @Nested
    @DisplayName("Create User")
    class CreateUser {
        @Test
        @DisplayName("Should create user with encoded password")
        void createUser_shouldCreateWithEncodedPassword() {
            when(passwordEncoder.encode("password")).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.createUser("test@test.com", "password", "Test", "User");

            assertThat(result).isNotNull();
            verify(passwordEncoder).encode("password");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find by email")
        void findByEmail_shouldReturnUser() {
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.findByEmail("test@test.com");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void findByEmail_shouldReturnEmpty() {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            Optional<User> result = userService.findByEmail("unknown@test.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find by login")
        void findByLogin_shouldReturnUser() {
            when(userRepository.findByLogin("test@test.com")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.findByLogin("test@test.com");

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnUser() {
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.findById("user-id");

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("Save Operation")
    class SaveOperation {
        @Test
        @DisplayName("Should save user")
        void save_shouldSaveUser() {
            when(userRepository.save(testUser)).thenReturn(testUser);

            User result = userService.save(testUser);

            assertThat(result).isNotNull();
            verify(userRepository).save(testUser);
        }
    }
}
