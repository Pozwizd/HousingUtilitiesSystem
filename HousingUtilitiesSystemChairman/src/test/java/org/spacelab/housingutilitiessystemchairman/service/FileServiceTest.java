package org.spacelab.housingutilitiessystemchairman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Tests")
class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "uploadDir", tempDir.toString());
    }

    @Nested
    @DisplayName("File Validation")
    class FileValidation {
        @Test
        @DisplayName("Should return true for valid file")
        void isValidFile_shouldReturnTrue_forValidFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(100L);
            when(file.getOriginalFilename()).thenReturn("test.jpg");

            boolean result = fileService.isValidFile(file);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null file")
        void isValidFile_shouldReturnFalse_forNullFile() {
            boolean result = fileService.isValidFile(null);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty file")
        void isValidFile_shouldReturnFalse_forEmptyFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            boolean result = fileService.isValidFile(file);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for zero size file")
        void isValidFile_shouldReturnFalse_forZeroSizeFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(0L);

            boolean result = fileService.isValidFile(file);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null filename")
        void isValidFile_shouldReturnFalse_forNullFilename() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(100L);
            when(file.getOriginalFilename()).thenReturn(null);

            boolean result = fileService.isValidFile(file);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty filename")
        void isValidFile_shouldReturnFalse_forEmptyFilename() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(100L);
            when(file.getOriginalFilename()).thenReturn("   ");

            boolean result = fileService.isValidFile(file);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("File Upload")
    class FileUpload {
        @Test
        @DisplayName("Should upload file successfully")
        void uploadFile_shouldUploadSuccessfully() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

            String result = fileService.uploadFile(file);

            assertThat(result).startsWith("uploads/");
            assertThat(result).endsWith("_test.jpg");
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFile_shouldReturnNull_forEmptyFile() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            String result = fileService.uploadFile(file);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should upload file if present")
        void uploadFileIfPresent_shouldUpload() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

            String result = fileService.uploadFileIfPresent(file);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should return null when file is null")
        void uploadFileIfPresent_shouldReturnNull_whenNull() throws IOException {
            String result = fileService.uploadFileIfPresent(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when file is empty")
        void uploadFileIfPresent_shouldReturnNull_whenEmpty() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            String result = fileService.uploadFileIfPresent(file);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("File Delete")
    class FileDelete {
        @Test
        @DisplayName("Should delete file successfully")
        void deleteFile_shouldDeleteSuccessfully() throws IOException {
            // Create a test file
            Path testFile = tempDir.resolve("test.jpg");
            Files.write(testFile, "test content".getBytes());

            boolean result = fileService.deleteFile("test.jpg");

            assertThat(result).isTrue();
            assertThat(Files.exists(testFile)).isFalse();
        }

        @Test
        @DisplayName("Should delete file with uploads prefix")
        void deleteFile_shouldDeleteWithUploadsPrefix() throws IOException {
            Path testFile = tempDir.resolve("test.jpg");
            Files.write(testFile, "test content".getBytes());

            boolean result = fileService.deleteFile("uploads/test.jpg");

            assertThat(result).isTrue();
            assertThat(Files.exists(testFile)).isFalse();
        }

        @Test
        @DisplayName("Should return false for null path")
        void deleteFile_shouldReturnFalse_forNullPath() throws IOException {
            boolean result = fileService.deleteFile(null);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty path")
        void deleteFile_shouldReturnFalse_forEmptyPath() throws IOException {
            boolean result = fileService.deleteFile("");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for non-existent file")
        void deleteFile_shouldReturnFalse_forNonExistentFile() throws IOException {
            boolean result = fileService.deleteFile("nonexistent.jpg");
            assertThat(result).isFalse();
        }
    }
}
