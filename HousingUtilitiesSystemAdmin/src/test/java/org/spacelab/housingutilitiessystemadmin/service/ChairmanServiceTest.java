package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.Chairman;
import org.spacelab.housingutilitiessystemadmin.exception.OperationException;
import org.spacelab.housingutilitiessystemadmin.mappers.ChairmanMapper;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanRequest;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponse;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponseTable;
import org.spacelab.housingutilitiessystemadmin.models.filters.chairman.ChairmanRequestTable;
import org.spacelab.housingutilitiessystemadmin.repository.ChairmanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChairmanService Tests")
class ChairmanServiceTest {

    @Mock
    private ChairmanRepository chairmanRepository;

    @Mock
    private ChairmanMapper chairmanMapper;

    @Mock
    private FileService fileService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ChairmanService chairmanService;

    private Chairman testChairman;
    private ChairmanResponse testChairmanResponse;
    private ChairmanRequest testChairmanRequest;

    @BeforeEach
    void setUp() {
        testChairman = new Chairman();
        testChairman.setId("chairman-id-123");
        testChairman.setEmail("chairman@test.com");
        testChairman.setLogin("chairman_login");

        testChairmanResponse = new ChairmanResponse();
        testChairmanResponse.setId("chairman-id-123");
        testChairmanResponse.setEmail("chairman@test.com");

        testChairmanRequest = new ChairmanRequest();
        testChairmanRequest.setEmail("chairman@test.com");
        testChairmanRequest.setLogin("chairman_login");
    }

    @Nested
    @DisplayName("Basic CRUD Tests")
    class BasicCrudTests {
        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnChairman() {
            when(chairmanRepository.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            Optional<Chairman> result = chairmanService.findById("chairman-id-123");
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("chairman-id-123");
        }

        @Test
        @DisplayName("Should return empty when not found")
        void findById_shouldReturnEmpty() {
            when(chairmanRepository.findById("nonexistent")).thenReturn(Optional.empty());
            Optional<Chairman> result = chairmanService.findById("nonexistent");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should save chairman")
        void save_shouldSaveChairman() {
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);
            Chairman result = chairmanService.save(testChairman);
            assertThat(result).isNotNull();
            verify(chairmanRepository).save(testChairman);
        }

        @Test
        @DisplayName("Should find all chairmen")
        void findAll_shouldReturnAllChairmen() {
            Chairman chairman2 = new Chairman();
            chairman2.setId("chairman-id-456");
            when(chairmanRepository.findAll()).thenReturn(Arrays.asList(testChairman, chairman2));
            List<Chairman> result = chairmanService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete by id")
        void deleteById_shouldDeleteChairman() {
            doNothing().when(chairmanRepository).deleteById("chairman-id-123");
            chairmanService.deleteById("chairman-id-123");
            verify(chairmanRepository).deleteById("chairman-id-123");
        }

        @Test
        @DisplayName("Should save all chairmen")
        void saveAll_shouldSaveAllChairmen() {
            List<Chairman> chairmen = Arrays.asList(testChairman, new Chairman());
            when(chairmanRepository.saveAll(chairmen)).thenReturn(chairmen);
            List<Chairman> result = chairmanService.saveAll(chairmen);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllChairmen() {
            doNothing().when(chairmanRepository).deleteAll();
            chairmanService.deleteAll();
            verify(chairmanRepository).deleteAll();
        }
    }

    @Nested
    @DisplayName("getChairmanById Tests")
    class GetChairmanByIdTests {
        @Test
        @DisplayName("Should return chairman response when found")
        void getChairmanById_shouldReturnChairmanResponse() {
            when(chairmanRepository.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);
            ChairmanResponse result = chairmanService.getChairmanById("chairman-id-123");
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("chairman-id-123");
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getChairmanById_shouldThrowException_whenNotFound() {
            when(chairmanRepository.findById("nonexistent")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> chairmanService.getChairmanById("nonexistent"))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }
    }

    @Nested
    @DisplayName("createChairman Tests")
    class CreateChairmanTests {
        @Test
        @DisplayName("Should create chairman successfully")
        void createChairman_shouldCreateSuccessfully() {
            when(chairmanRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(chairmanRepository.findByLogin(anyString())).thenReturn(Optional.empty());
            when(chairmanMapper.toEntity(testChairmanRequest)).thenReturn(testChairman);
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);

            ChairmanResponse result = chairmanService.createChairman(testChairmanRequest);

            assertThat(result).isNotNull();
            verify(chairmanRepository).save(any(Chairman.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createChairman_shouldThrowException_whenEmailExists() {
            when(chairmanRepository.findByEmail(anyString())).thenReturn(Optional.of(testChairman));

            assertThatThrownBy(() -> chairmanService.createChairman(testChairmanRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("уже существует");
        }

        @Test
        @DisplayName("Should throw exception when login already exists")
        void createChairman_shouldThrowException_whenLoginExists() {
            when(chairmanRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(chairmanRepository.findByLogin(anyString())).thenReturn(Optional.of(testChairman));

            assertThatThrownBy(() -> chairmanService.createChairman(testChairmanRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("уже существует");
        }

        @Test
        @DisplayName("Should create chairman with password")
        void createChairman_withPassword_shouldSetPassword() {
            testChairmanRequest.setPassword("password123");
            when(chairmanRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(chairmanRepository.findByLogin(anyString())).thenReturn(Optional.empty());
            when(chairmanMapper.toEntity(testChairmanRequest)).thenReturn(testChairman);
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);

            chairmanService.createChairman(testChairmanRequest);

            verify(chairmanRepository).save(any(Chairman.class));
        }

        @Test
        @DisplayName("Should create chairman with photo")
        void createChairman_withPhoto_shouldUploadPhoto() throws IOException {
            testChairmanRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(chairmanRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(chairmanRepository.findByLogin(anyString())).thenReturn(Optional.empty());
            when(chairmanMapper.toEntity(testChairmanRequest)).thenReturn(testChairman);
            when(fileService.uploadFile(multipartFile)).thenReturn("uploads/photo.jpg");
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);

            chairmanService.createChairman(testChairmanRequest);

            verify(fileService).uploadFile(multipartFile);
        }

        @Test
        @DisplayName("Should throw exception when photo upload fails")
        void createChairman_shouldThrowException_whenPhotoUploadFails() throws IOException {
            testChairmanRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(chairmanRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(chairmanRepository.findByLogin(anyString())).thenReturn(Optional.empty());
            when(chairmanMapper.toEntity(testChairmanRequest)).thenReturn(testChairman);
            when(fileService.uploadFile(multipartFile)).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> chairmanService.createChairman(testChairmanRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Ошибка при загрузке фото");
        }
    }

    @Nested
    @DisplayName("updateChairman Tests")
    class UpdateChairmanTests {
        @Test
        @DisplayName("Should update chairman successfully")
        void updateChairman_shouldUpdateSuccessfully() {
            when(chairmanRepository.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);

            ChairmanResponse result = chairmanService.updateChairman("chairman-id-123", testChairmanRequest);

            assertThat(result).isNotNull();
            verify(chairmanMapper).partialUpdate(testChairmanRequest, testChairman);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void updateChairman_shouldThrowException_whenNotFound() {
            when(chairmanRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chairmanService.updateChairman("nonexistent", testChairmanRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }

        @Test
        @DisplayName("Should update chairman with new password")
        void updateChairman_withPassword_shouldUpdatePassword() {
            testChairmanRequest.setPassword("newPassword");
            when(chairmanRepository.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);

            chairmanService.updateChairman("chairman-id-123", testChairmanRequest);

            verify(chairmanRepository).save(any(Chairman.class));
        }

        @Test
        @DisplayName("Should update chairman with new photo and delete old")
        void updateChairman_withNewPhoto_shouldDeleteOldAndUploadNew() throws IOException {
            testChairman.setPhoto("old-photo.jpg");
            testChairmanRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(chairmanRepository.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            when(fileService.uploadFile(multipartFile)).thenReturn("new-photo.jpg");
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);

            chairmanService.updateChairman("chairman-id-123", testChairmanRequest);

            verify(fileService).deleteFile("old-photo.jpg");
            verify(fileService).uploadFile(multipartFile);
        }

        @Test
        @DisplayName("Should throw exception when photo upload fails during update")
        void updateChairman_shouldThrowException_whenPhotoUploadFails() throws IOException {
            testChairmanRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(chairmanRepository.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            when(fileService.uploadFile(multipartFile)).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> chairmanService.updateChairman("chairman-id-123", testChairmanRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Ошибка при загрузке фото");
        }
    }

    @Nested
    @DisplayName("deleteChairman Tests")
    class DeleteChairmanTests {
        @Test
        @DisplayName("Should delete chairman successfully")
        void deleteChairman_shouldDeleteSuccessfully() {
            when(chairmanRepository.existsById("chairman-id-123")).thenReturn(true);
            doNothing().when(chairmanRepository).deleteById("chairman-id-123");

            boolean result = chairmanService.deleteChairman("chairman-id-123");

            assertThat(result).isTrue();
            verify(chairmanRepository).deleteById("chairman-id-123");
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void deleteChairman_shouldThrowException_whenNotFound() {
            when(chairmanRepository.existsById("nonexistent")).thenReturn(false);

            assertThatThrownBy(() -> chairmanService.deleteChairman("nonexistent"))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }
    }

    @Nested
    @DisplayName("getChairmenTable Tests")
    class GetChairmenTableTests {
        @Test
        @DisplayName("Should get chairmen table")
        void getChairmenTable_shouldReturnTable() {
            ChairmanRequestTable requestTable = new ChairmanRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            Page<Chairman> chairmenPage = new PageImpl<>(List.of(testChairman));
            Page<ChairmanResponseTable> responsePage = new PageImpl<>(List.of(new ChairmanResponseTable()));

            when(chairmanRepository.findChairmenWithFilters(requestTable)).thenReturn(chairmenPage);
            when(chairmanMapper.toResponseTablePage(chairmenPage)).thenReturn(responsePage);

            Page<ChairmanResponseTable> result = chairmanService.getChairmenTable(requestTable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("searchByName Tests")
    class SearchByNameTests {
        @Test
        @DisplayName("Should search by name")
        void searchByName_shouldReturnResults() {
            when(chairmanRepository.findByFullNameContaining("Test")).thenReturn(List.of(testChairman));
            when(chairmanMapper.toResponseList(any())).thenReturn(List.of(testChairmanResponse));

            List<ChairmanResponse> result = chairmanService.searchByName("Test");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return all when name is null")
        void searchByName_shouldReturnAll_whenNameIsNull() {
            when(chairmanRepository.findAll()).thenReturn(List.of(testChairman));
            when(chairmanMapper.toResponseList(any())).thenReturn(List.of(testChairmanResponse));

            List<ChairmanResponse> result = chairmanService.searchByName(null);

            assertThat(result).hasSize(1);
            verify(chairmanRepository).findAll();
        }

        @Test
        @DisplayName("Should return all when name is empty")
        void searchByName_shouldReturnAll_whenNameIsEmpty() {
            when(chairmanRepository.findAll()).thenReturn(List.of(testChairman));
            when(chairmanMapper.toResponseList(any())).thenReturn(List.of(testChairmanResponse));

            List<ChairmanResponse> result = chairmanService.searchByName("   ");

            assertThat(result).hasSize(1);
            verify(chairmanRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Async Methods Tests")
    class AsyncMethodsTests {
        @Test
        @DisplayName("Should find by id async")
        void findByIdAsync_shouldReturnChairmanResponse() throws ExecutionException, InterruptedException {
            when(chairmanRepository.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);

            CompletableFuture<ChairmanResponse> future = chairmanService.findByIdAsync("chairman-id-123");
            ChairmanResponse result = future.get();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("chairman-id-123");
        }

        @Test
        @DisplayName("Should throw exception when not found async")
        void findByIdAsync_shouldThrowException_whenNotFound() {
            when(chairmanRepository.findById("nonexistent")).thenReturn(Optional.empty());

            CompletableFuture<ChairmanResponse> future = chairmanService.findByIdAsync("nonexistent");

            assertThatThrownBy(() -> future.get())
                    .hasCauseInstanceOf(OperationException.class);
        }

        @Test
        @DisplayName("Should find by email async")
        void findByEmailAsync_shouldReturnOptional() throws ExecutionException, InterruptedException {
            when(chairmanRepository.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));

            CompletableFuture<Optional<Chairman>> future = chairmanService.findByEmailAsync("chairman@test.com");
            Optional<Chairman> result = future.get();

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should create chairman async")
        void createChairmanAsync_shouldCreateSuccessfully() throws ExecutionException, InterruptedException {
            when(chairmanRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(chairmanRepository.findByLogin(anyString())).thenReturn(Optional.empty());
            when(chairmanMapper.toEntity(testChairmanRequest)).thenReturn(testChairman);
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);

            CompletableFuture<ChairmanResponse> future = chairmanService.createChairmanAsync(testChairmanRequest);
            ChairmanResponse result = future.get();

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should update chairman async")
        void updateChairmanAsync_shouldUpdateSuccessfully() throws ExecutionException, InterruptedException {
            testChairmanRequest.setId("chairman-id-123");
            when(chairmanRepository.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            when(chairmanRepository.save(any(Chairman.class))).thenReturn(testChairman);
            when(chairmanMapper.mapChairmanToResponse(testChairman)).thenReturn(testChairmanResponse);

            CompletableFuture<ChairmanResponse> future = chairmanService.updateChairmanAsync(testChairmanRequest);
            ChairmanResponse result = future.get();

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should delete by id async")
        void deleteByIdAsync_shouldDeleteSuccessfully() throws ExecutionException, InterruptedException {
            when(chairmanRepository.existsById("chairman-id-123")).thenReturn(true);
            doNothing().when(chairmanRepository).deleteById("chairman-id-123");

            CompletableFuture<Boolean> future = chairmanService.deleteByIdAsync("chairman-id-123");
            Boolean result = future.get();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should get pageable chairmen async")
        void getPageableChairmenAsync_shouldReturnPage() throws ExecutionException, InterruptedException {
            Page<Chairman> chairmenPage = new PageImpl<>(List.of(testChairman));
            Page<ChairmanResponseTable> responsePage = new PageImpl<>(List.of(new ChairmanResponseTable()));

            when(chairmanRepository.findChairmenWithFilters(any(ChairmanRequestTable.class))).thenReturn(chairmenPage);
            when(chairmanMapper.toResponseTablePage(any())).thenReturn(responsePage);

            CompletableFuture<Page<ChairmanResponseTable>> future = chairmanService.getPageableChairmenAsync(
                    0, 10, "Last", "First", "Middle", "123456789", "email@test.com", "login", "active");
            Page<ChairmanResponseTable> result = future.get();

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should get pageable chairmen async with null params")
        void getPageableChairmenAsync_withNullParams_shouldReturnPage() throws ExecutionException, InterruptedException {
            Page<Chairman> chairmenPage = new PageImpl<>(List.of(testChairman));
            Page<ChairmanResponseTable> responsePage = new PageImpl<>(List.of(new ChairmanResponseTable()));

            when(chairmanRepository.findChairmenWithFilters(any(ChairmanRequestTable.class))).thenReturn(chairmenPage);
            when(chairmanMapper.toResponseTablePage(any())).thenReturn(responsePage);

            CompletableFuture<Page<ChairmanResponseTable>> future = chairmanService.getPageableChairmenAsync(
                    0, 10, null, null, null, null, null, null, null);
            Page<ChairmanResponseTable> result = future.get();

            assertThat(result).isNotNull();
        }
    }
}
