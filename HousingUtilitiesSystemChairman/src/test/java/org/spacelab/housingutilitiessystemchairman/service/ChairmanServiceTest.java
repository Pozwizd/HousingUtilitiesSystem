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
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChairmanService Tests")
class ChairmanServiceTest {

    @Mock
    private ChairmanRepository chairmanRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ChairmanService chairmanService;

    private Chairman testChairman;

    @BeforeEach
    void setUp() {
        testChairman = new Chairman();
        testChairman.setId("chairman-id");
        testChairman.setEmail("chairman@test.com");
        testChairman.setLogin("chairman@test.com");
        testChairman.setPassword("encodedPassword");
        testChairman.setEnabled(true);
    }

    @Nested
    @DisplayName("Create Chairman")
    class CreateChairman {
        @Test
        @DisplayName("Should create chairman with email and password")
        void createChairman_shouldCreateWithEmailAndPassword() {
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);

            Chairman result = chairmanService.createChairman("chairman@test.com", "password");

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("chairman@test.com");
            verify(passwordEncoder).encode("password");
            verify(chairmanRepository).save(any(Chairman.class));
        }

        @Test
        @DisplayName("Should create chairman with all details")
        void createChairman_shouldCreateWithAllDetails() {
            testChairman.setFirstName("John");
            testChairman.setLastName("Doe");
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);

            Chairman result = chairmanService.createChairman("chairman@test.com", "password", "John", "Doe");

            assertThat(result).isNotNull();
            verify(chairmanRepository).save(any(Chairman.class));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find by email when exists")
        void findByEmail_shouldReturnChairman() {
            when(chairmanRepository.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));

            Optional<Chairman> result = chairmanService.findByEmail("chairman@test.com");

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void findByEmail_shouldReturnEmpty() {
            when(chairmanRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            Optional<Chairman> result = chairmanService.findByEmail("unknown@test.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find by login when exists")
        void findByLogin_shouldReturnChairman() {
            when(chairmanRepository.findByLogin("chairman@test.com")).thenReturn(Optional.of(testChairman));

            Optional<Chairman> result = chairmanService.findByLogin("chairman@test.com");

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when login not found")
        void findByLogin_shouldReturnEmpty() {
            when(chairmanRepository.findByLogin(anyString())).thenReturn(Optional.empty());

            Optional<Chairman> result = chairmanService.findByLogin("unknown");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find by id when exists")
        void findById_shouldReturnChairman() {
            when(chairmanRepository.findById("chairman-id")).thenReturn(Optional.of(testChairman));

            Optional<Chairman> result = chairmanService.findById("chairman-id");

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when id not found")
        void findById_shouldReturnEmpty() {
            when(chairmanRepository.findById(anyString())).thenReturn(Optional.empty());

            Optional<Chairman> result = chairmanService.findById("unknown-id");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Password Operations")
    class PasswordOperations {
        @Test
        @DisplayName("Should return true for matching password")
        void checkPassword_shouldReturnTrue() {
            when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

            boolean result = chairmanService.checkPassword("rawPassword", "encodedPassword");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-matching password")
        void checkPassword_shouldReturnFalse() {
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            boolean result = chairmanService.checkPassword("wrongPassword", "encodedPassword");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        @Test
        @DisplayName("Should save chairman")
        void save_shouldSaveChairman() {
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);

            Chairman result = chairmanService.save(testChairman);

            assertThat(result).isNotNull();
            verify(chairmanRepository).save(testChairman);
        }

        @Test
        @DisplayName("Should save all chairmen")
        void saveAll_shouldSaveAllChairmen() {
            List<Chairman> chairmen = Arrays.asList(testChairman, new Chairman());
            when(chairmanRepository.saveAll(any(Iterable.class))).thenReturn(chairmen);

            Iterable<Chairman> result = chairmanService.saveAll(chairmen);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAll() {
            doNothing().when(chairmanRepository).deleteAll();

            chairmanService.deleteAll();

            verify(chairmanRepository).deleteAll();
        }

        @Test
        @DisplayName("Should delete by id")
        void deleteById_shouldDeleteById() {
            doNothing().when(chairmanRepository).deleteById("chairman-id");

            chairmanService.deleteById("chairman-id");

            verify(chairmanRepository).deleteById("chairman-id");
        }

        @Test
        @DisplayName("Should check exists by id - true")
        void existsById_shouldReturnTrue() {
            when(chairmanRepository.existsById("chairman-id")).thenReturn(true);

            boolean result = chairmanService.existsById("chairman-id");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should check exists by id - false")
        void existsById_shouldReturnFalse() {
            when(chairmanRepository.existsById("unknown")).thenReturn(false);

            boolean result = chairmanService.existsById("unknown");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Online Status")
    class OnlineStatus {
        @Test
        @DisplayName("Should update online status when chairman found")
        void updateOnlineStatus_shouldUpdate() {
            when(chairmanRepository.findByLogin("chairman@test.com")).thenReturn(Optional.of(testChairman));
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);

            chairmanService.updateOnlineStatus("chairman@test.com", true);

            verify(chairmanRepository).save(any(Chairman.class));
        }

        @Test
        @DisplayName("Should not update when chairman not found")
        void updateOnlineStatus_shouldNotUpdate_whenNotFound() {
            when(chairmanRepository.findByLogin("unknown")).thenReturn(Optional.empty());

            chairmanService.updateOnlineStatus("unknown", true);

            verify(chairmanRepository, never()).save(any(Chairman.class));
        }
    }
}
