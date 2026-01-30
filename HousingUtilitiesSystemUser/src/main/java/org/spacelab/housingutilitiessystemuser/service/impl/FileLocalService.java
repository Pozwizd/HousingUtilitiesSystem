package org.spacelab.housingutilitiessystemuser.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class FileLocalService implements FileService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public boolean isValidFile(MultipartFile file) {
        return file != null &&
                !file.isEmpty() &&
                file.getSize() > 0 &&
                file.getOriginalFilename() != null &&
                !file.getOriginalFilename().trim().isEmpty();
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "/uploads/" + fileName;
        log.debug("Uploaded file locally: {}", fileUrl);

        return fileUrl;
    }

    @Override
    public String uploadFileIfPresent(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return uploadFile(file);
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error deleting file locally: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        return Files.exists(filePath);
    }
}
