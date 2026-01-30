package org.spacelab.housingutilitiessystemuser.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    boolean isValidFile(MultipartFile file);

    String uploadFile(MultipartFile file) throws IOException;

    String uploadFileIfPresent(MultipartFile file) throws IOException;

    boolean deleteFile(String fileUrl);

    boolean fileExists(String fileUrl);
}
