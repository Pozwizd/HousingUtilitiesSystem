package org.spacelab.housingutilitiessystemuser.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Tests")
class FileServiceTest {

    @Mock
    private Storage storage;

    @Mock
    private Blob blob;

    private FileService fileService;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String PUBLIC_URL = "https://storage.googleapis.com";

    @BeforeEach
    void setUp() {
        fileService = new FileService(storage);
        ReflectionTestUtils.setField(fileService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(fileService, "publicUrl", PUBLIC_URL);
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
    }

    @Nested
    @DisplayName("uploadFile Tests")
    class UploadFileTests {

        @Test
        @DisplayName("Should upload file to GCS and return URL")
        void uploadFile_shouldUploadAndReturnUrl() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getBytes()).thenReturn("file content".getBytes());
            when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

            String result = fileService.uploadFile(file);

            assertThat(result).startsWith(PUBLIC_URL + "/" + BUCKET_NAME + "/uploads/");
            assertThat(result).endsWith("_test.jpg");
            verify(storage).create(any(BlobInfo.class), any(byte[].class));
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFile_shouldReturnNull_forEmptyFile() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            String result = fileService.uploadFile(file);

            assertThat(result).isNull();
            verify(storage, never()).create(any(BlobInfo.class), any(byte[].class));
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("Should delete existing file and return true")
        void deleteFile_shouldDeleteAndReturnTrue() {
            String fileUrl = PUBLIC_URL + "/" + BUCKET_NAME + "/uploads/test.jpg";
            when(storage.delete(any(BlobId.class))).thenReturn(true);

            boolean result = fileService.deleteFile(fileUrl);

            assertThat(result).isTrue();
            verify(storage).delete(any(BlobId.class));
        }

        @Test
        @DisplayName("Should return false for null URL")
        void deleteFile_shouldReturnFalse_forNullUrl() {
            boolean result = fileService.deleteFile(null);

            assertThat(result).isFalse();
            verify(storage, never()).delete(any(BlobId.class));
        }
    }
}
