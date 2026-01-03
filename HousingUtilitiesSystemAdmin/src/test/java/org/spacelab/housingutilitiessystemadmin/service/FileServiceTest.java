package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Tests")
class FileServiceTest {

    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileService = new FileService();
        ReflectionTestUtils.setField(fileService, "uploadDir", tempDir.toString());
    }

    @Nested
    @DisplayName("isValidFile Tests")
    class IsValidFileTests {

        @Test
        @DisplayName("Should return true for valid file")
        void isValidFile_shouldReturnTrue_forValidFile() {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn("test.jpg");

            // When
            boolean result = fileService.isValidFile(file);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null file")
        void isValidFile_shouldReturnFalse_forNullFile() {
            // When
            boolean result = fileService.isValidFile(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty file")
        void isValidFile_shouldReturnFalse_forEmptyFile() {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            // When
            boolean result = fileService.isValidFile(file);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for file with zero size")
        void isValidFile_shouldReturnFalse_forZeroSizeFile() {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(0L);

            // When
            boolean result = fileService.isValidFile(file);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for file with null filename")
        void isValidFile_shouldReturnFalse_forNullFilename() {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn(null);

            // When
            boolean result = fileService.isValidFile(file);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for file with empty filename")
        void isValidFile_shouldReturnFalse_forEmptyFilename() {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn("   ");

            // When
            boolean result = fileService.isValidFile(file);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("uploadFile Tests")
    class UploadFileTests {

        @Test
        @DisplayName("Should upload file and return path")
        void uploadFile_shouldUploadAndReturnPath() throws IOException {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream("file content".getBytes()));

            // When
            String result = fileService.uploadFile(file);

            // Then
            assertThat(result).startsWith("uploads/");
            assertThat(result).endsWith("_test.jpg");
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFile_shouldReturnNull_forEmptyFile() throws IOException {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            // When
            String result = fileService.uploadFile(file);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("uploadFileIfPresent Tests")
    class UploadFileIfPresentTests {

        @Test
        @DisplayName("Should return null for null file")
        void uploadFileIfPresent_shouldReturnNull_forNullFile() throws IOException {
            // When
            String result = fileService.uploadFileIfPresent(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFileIfPresent_shouldReturnNull_forEmptyFile() throws IOException {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            // When
            String result = fileService.uploadFileIfPresent(file);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should upload file if present")
        void uploadFileIfPresent_shouldUpload_whenFilePresent() throws IOException {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream("file content".getBytes()));

            // When
            String result = fileService.uploadFileIfPresent(file);

            // Then
            assertThat(result).startsWith("uploads/");
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("Should delete existing file and return true")
        void deleteFile_shouldDeleteAndReturnTrue() throws IOException {
            // Given
            Path testFile = tempDir.resolve("test.jpg");
            Files.createFile(testFile);
            assertThat(Files.exists(testFile)).isTrue();

            // When
            boolean result = fileService.deleteFile("test.jpg");

            // Then
            assertThat(result).isTrue();
            assertThat(Files.exists(testFile)).isFalse();
        }

        @Test
        @DisplayName("Should return false when file does not exist")
        void deleteFile_shouldReturnFalse_whenFileNotExists() throws IOException {
            // When
            boolean result = fileService.deleteFile("nonexistent.jpg");

            // Then
            assertThat(result).isFalse();
        }
    }
}
