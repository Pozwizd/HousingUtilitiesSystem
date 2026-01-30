package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.service.impl.FileLocalService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileLocalService Tests")
class FileLocalServiceTest {

    private FileLocalService fileLocalService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        fileLocalService = new FileLocalService();
        tempDir = Files.createTempDirectory("file-test");
        ReflectionTestUtils.setField(fileLocalService, "uploadDir", tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temp directory
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
        }
    }

    @Nested
    @DisplayName("isValidFile Tests")
    class IsValidFileTests {

        @Test
        @DisplayName("Should return true for valid file")
        void isValidFile_shouldReturnTrue_forValidFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn("test.jpg");

            boolean result = fileLocalService.isValidFile(file);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null file")
        void isValidFile_shouldReturnFalse_forNullFile() {
            boolean result = fileLocalService.isValidFile(null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty file")
        void isValidFile_shouldReturnFalse_forEmptyFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            boolean result = fileLocalService.isValidFile(file);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for file with zero size")
        void isValidFile_shouldReturnFalse_forZeroSizeFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(0L);

            boolean result = fileLocalService.isValidFile(file);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for file with null filename")
        void isValidFile_shouldReturnFalse_forNullFilename() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn(null);

            boolean result = fileLocalService.isValidFile(file);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for file with empty filename")
        void isValidFile_shouldReturnFalse_forEmptyFilename() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn("   ");

            boolean result = fileLocalService.isValidFile(file);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("uploadFile Tests")
    class UploadFileTests {

        @Test
        @DisplayName("Should upload file locally and return URL")
        void uploadFile_shouldUploadAndReturnUrl() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream("file content".getBytes()));

            String result = fileLocalService.uploadFile(file);

            assertThat(result).startsWith("uploads/");
            assertThat(result).endsWith("_test.jpg");
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFile_shouldReturnNull_forEmptyFile() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            String result = fileLocalService.uploadFile(file);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("uploadFileIfPresent Tests")
    class UploadFileIfPresentTests {

        @Test
        @DisplayName("Should return null for null file")
        void uploadFileIfPresent_shouldReturnNull_forNullFile() throws IOException {
            String result = fileLocalService.uploadFileIfPresent(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFileIfPresent_shouldReturnNull_forEmptyFile() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            String result = fileLocalService.uploadFileIfPresent(file);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should upload file if present")
        void uploadFileIfPresent_shouldUpload_whenFilePresent() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream("file content".getBytes()));

            String result = fileLocalService.uploadFileIfPresent(file);

            assertThat(result).startsWith("uploads/");
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("Should delete existing file and return true")
        void deleteFile_shouldDeleteAndReturnTrue() throws IOException {
            // Create a test file
            Path testFile = tempDir.resolve("test.jpg");
            Files.write(testFile, "test content".getBytes());

            boolean result = fileLocalService.deleteFile("test.jpg");

            assertThat(result).isTrue();
            assertThat(Files.exists(testFile)).isFalse();
        }

        @Test
        @DisplayName("Should return false when file does not exist")
        void deleteFile_shouldReturnFalse_whenFileNotExists() throws IOException {
            boolean result = fileLocalService.deleteFile("nonexistent.jpg");

            assertThat(result).isFalse();
        }
    }
}
