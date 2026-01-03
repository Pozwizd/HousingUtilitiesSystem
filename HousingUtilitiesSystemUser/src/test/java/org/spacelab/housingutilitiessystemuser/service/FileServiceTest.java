package org.spacelab.housingutilitiessystemuser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Tests")
class FileServiceTest {

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "uploadDir", tempDir.toString());
    }

    @Nested
    @DisplayName("Is Valid File")
    class IsValidFile {
        @Test
        @DisplayName("Should return true for valid file")
        void isValidFile_shouldReturnTrue() {
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getSize()).thenReturn(1024L);
            when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");

            boolean result = fileService.isValidFile(multipartFile);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null file")
        void isValidFile_shouldReturnFalseForNull() {
            boolean result = fileService.isValidFile(null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty file")
        void isValidFile_shouldReturnFalseForEmpty() {
            when(multipartFile.isEmpty()).thenReturn(true);

            boolean result = fileService.isValidFile(multipartFile);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for zero size")
        void isValidFile_shouldReturnFalseForZeroSize() {
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getSize()).thenReturn(0L);

            boolean result = fileService.isValidFile(multipartFile);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null filename")
        void isValidFile_shouldReturnFalseForNullFilename() {
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getSize()).thenReturn(1024L);
            when(multipartFile.getOriginalFilename()).thenReturn(null);

            boolean result = fileService.isValidFile(multipartFile);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty filename")
        void isValidFile_shouldReturnFalseForEmptyFilename() {
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getSize()).thenReturn(1024L);
            when(multipartFile.getOriginalFilename()).thenReturn("   ");

            boolean result = fileService.isValidFile(multipartFile);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Upload File")
    class UploadFile {
        @Test
        @DisplayName("Should upload file successfully")
        void uploadFile_shouldUpload() throws IOException {
            byte[] content = "test content".getBytes();
            InputStream inputStream = new ByteArrayInputStream(content);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
            when(multipartFile.getInputStream()).thenReturn(inputStream);

            String result = fileService.uploadFile(multipartFile);

            assertThat(result).isNotNull();
            assertThat(result).startsWith("uploads/");
            assertThat(result).endsWith("_test.jpg");
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFile_shouldReturnNullForEmpty() throws IOException {
            when(multipartFile.isEmpty()).thenReturn(true);

            String result = fileService.uploadFile(multipartFile);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Upload File If Present")
    class UploadFileIfPresent {
        @Test
        @DisplayName("Should return null for null file")
        void uploadFileIfPresent_shouldReturnNullForNull() throws IOException {
            String result = fileService.uploadFileIfPresent(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFileIfPresent_shouldReturnNullForEmpty() throws IOException {
            when(multipartFile.isEmpty()).thenReturn(true);

            String result = fileService.uploadFileIfPresent(multipartFile);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should upload when file is present")
        void uploadFileIfPresent_shouldUploadWhenPresent() throws IOException {
            byte[] content = "test content".getBytes();
            InputStream inputStream = new ByteArrayInputStream(content);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
            when(multipartFile.getInputStream()).thenReturn(inputStream);

            String result = fileService.uploadFileIfPresent(multipartFile);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Delete File")
    class DeleteFile {
        @Test
        @DisplayName("Should return false for null filename")
        void deleteFile_shouldReturnFalseForNull() throws IOException {
            boolean result = fileService.deleteFile(null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty filename")
        void deleteFile_shouldReturnFalseForEmpty() throws IOException {
            boolean result = fileService.deleteFile("");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should delete existing file")
        void deleteFile_shouldDeleteExisting() throws IOException {
            Path testFile = tempDir.resolve("test.txt");
            Files.createFile(testFile);

            boolean result = fileService.deleteFile(testFile.toString());

            assertThat(result).isTrue();
            assertThat(Files.exists(testFile)).isFalse();
        }

        @Test
        @DisplayName("Should return false for non-existing file")
        void deleteFile_shouldReturnFalseForNonExisting() throws IOException {
            boolean result = fileService.deleteFile(tempDir.resolve("nonexistent.txt").toString());

            assertThat(result).isFalse();
        }
    }
}
