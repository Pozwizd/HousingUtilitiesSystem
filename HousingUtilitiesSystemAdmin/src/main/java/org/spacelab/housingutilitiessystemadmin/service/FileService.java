package org.spacelab.housingutilitiessystemadmin.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final Storage storage;

    @Value("${gcs.bucket-name:housing-system-uploads}")
    private String bucketName;

    @Value("${gcs.public-url:https://storage.googleapis.com}")
    private String publicUrl;

    @Named("isValidFile")
    public boolean isValidFile(MultipartFile file) {
        return file != null &&
                !file.isEmpty() &&
                file.getSize() > 0 &&
                file.getOriginalFilename() != null &&
                !file.getOriginalFilename().trim().isEmpty();
    }

    @Named("uploadFile")
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
        String objectName = "uploads/" + fileName;

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        String fileUrl = publicUrl + "/" + bucketName + "/" + objectName;
        log.debug("Uploaded file to GCS: {}", fileUrl);

        return fileUrl;
    }

    @Named("uploadFileIfPresent")
    public String uploadFileIfPresent(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return uploadFile(file);
    }

    @Named("deleteFile")
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            // Extract object name from URL
            // URL format: https://storage.googleapis.com/bucket-name/uploads/filename
            String objectName = extractObjectName(fileUrl);
            if (objectName == null) {
                log.warn("Could not extract object name from URL: {}", fileUrl);
                return false;
            }

            BlobId blobId = BlobId.of(bucketName, objectName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.debug("Deleted file from GCS: {}", fileUrl);
            } else {
                log.warn("File not found in GCS: {}", fileUrl);
            }

            return deleted;
        } catch (Exception e) {
            log.error("Error deleting file from GCS: {}", fileUrl, e);
            return false;
        }
    }

    private String extractObjectName(String fileUrl) {
        if (fileUrl == null) {
            return null;
        }

        // Handle full GCS URL
        String prefix = publicUrl + "/" + bucketName + "/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }

        // Handle legacy local path (uploads/filename)
        if (fileUrl.startsWith("uploads/")) {
            return fileUrl;
        }

        return null;
    }

    /**
     * Check if file exists in GCS
     */
    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        String objectName = extractObjectName(fileUrl);
        if (objectName == null) {
            return false;
        }

        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        return blob != null && blob.exists();
    }
}
