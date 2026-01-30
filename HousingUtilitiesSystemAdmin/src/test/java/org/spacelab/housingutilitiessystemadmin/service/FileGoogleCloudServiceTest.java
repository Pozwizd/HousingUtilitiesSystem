package org.spacelab.housingutilitiessystemadmin.service;

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
import org.spacelab.housingutilitiessystemadmin.service.impl.FileGoogleCloudService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Tests")
class FileGoogleCloudServiceTest {

    @Mock
    private Storage storage;

    @Mock
    private Blob blob;

    private FileGoogleCloudService fileGoogleCloudService;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String PUBLIC_URL = "https://storage.googleapis.com";

    @BeforeEach
    void setUp() {
        fileGoogleCloudService = new FileGoogleCloudService(storage);
        ReflectionTestUtils.setField(fileGoogleCloudService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(fileGoogleCloudService, "publicUrl", PUBLIC_URL);
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

            boolean result = fileGoogleCloudService.isValidFile(file);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null file")
        void isValidFile_shouldReturnFalse_forNullFile() {
            boolean result = fileGoogleCloudService.isValidFile(null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty file")
        void isValidFile_shouldReturnFalse_forEmptyFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            boolean result = fileGoogleCloudService.isValidFile(file);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for file with zero size")
        void isValidFile_shouldReturnFalse_forZeroSizeFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(0L);

            boolean result = fileGoogleCloudService.isValidFile(file);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for file with null filename")
        void isValidFile_shouldReturnFalse_forNullFilename() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn(null);

            boolean result = fileGoogleCloudService.isValidFile(file);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for file with empty filename")
        void isValidFile_shouldReturnFalse_forEmptyFilename() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn("   ");

            boolean result = fileGoogleCloudService.isValidFile(file);

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

            String result = fileGoogleCloudService.uploadFile(file);

            assertThat(result).startsWith(PUBLIC_URL + "/" + BUCKET_NAME + "/uploads/");
            assertThat(result).endsWith("_test.jpg");
            verify(storage).create(any(BlobInfo.class), any(byte[].class));
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFile_shouldReturnNull_forEmptyFile() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            String result = fileGoogleCloudService.uploadFile(file);

            assertThat(result).isNull();
            verify(storage, never()).create(any(BlobInfo.class), any(byte[].class));
        }
    }

    @Nested
    @DisplayName("uploadFileIfPresent Tests")
    class UploadFileIfPresentTests {

        @Test
        @DisplayName("Should return null for null file")
        void uploadFileIfPresent_shouldReturnNull_forNullFile() throws IOException {
            String result = fileGoogleCloudService.uploadFileIfPresent(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty file")
        void uploadFileIfPresent_shouldReturnNull_forEmptyFile() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            String result = fileGoogleCloudService.uploadFileIfPresent(file);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should upload file if present")
        void uploadFileIfPresent_shouldUpload_whenFilePresent() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getBytes()).thenReturn("file content".getBytes());
            when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

            String result = fileGoogleCloudService.uploadFileIfPresent(file);

            assertThat(result).startsWith(PUBLIC_URL + "/" + BUCKET_NAME + "/uploads/");
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

            boolean result = fileGoogleCloudService.deleteFile(fileUrl);

            assertThat(result).isTrue();
            verify(storage).delete(any(BlobId.class));
        }

        @Test
        @DisplayName("Should return false when file does not exist")
        void deleteFile_shouldReturnFalse_whenFileNotExists() {
            String fileUrl = PUBLIC_URL + "/" + BUCKET_NAME + "/uploads/nonexistent.jpg";
            when(storage.delete(any(BlobId.class))).thenReturn(false);

            boolean result = fileGoogleCloudService.deleteFile(fileUrl);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null URL")
        void deleteFile_shouldReturnFalse_forNullUrl() {
            boolean result = fileGoogleCloudService.deleteFile(null);

            assertThat(result).isFalse();
            verify(storage, never()).delete(any(BlobId.class));
        }

        @Test
        @DisplayName("Should return false for empty URL")
        void deleteFile_shouldReturnFalse_forEmptyUrl() {
            boolean result = fileGoogleCloudService.deleteFile("");

            assertThat(result).isFalse();
            verify(storage, never()).delete(any(BlobId.class));
        }
    }

    @Nested
    @DisplayName("fileExists Tests")
    class FileExistsTests {

        @Test
        @DisplayName("Should return true when file exists")
        void fileExists_shouldReturnTrue_whenFileExists() {
            String fileUrl = PUBLIC_URL + "/" + BUCKET_NAME + "/uploads/test.jpg";
            when(storage.get(any(BlobId.class))).thenReturn(blob);
            when(blob.exists()).thenReturn(true);

            boolean result = fileGoogleCloudService.fileExists(fileUrl);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when file does not exist")
        void fileExists_shouldReturnFalse_whenFileNotExists() {
            String fileUrl = PUBLIC_URL + "/" + BUCKET_NAME + "/uploads/nonexistent.jpg";
            when(storage.get(any(BlobId.class))).thenReturn(null);

            boolean result = fileGoogleCloudService.fileExists(fileUrl);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null URL")
        void fileExists_shouldReturnFalse_forNullUrl() {
            boolean result = fileGoogleCloudService.fileExists(null);

            assertThat(result).isFalse();
        }
    }
}
