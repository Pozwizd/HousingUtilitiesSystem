package org.spacelab.housingutilitiessystemchairman.service;

import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    @Named("isValidFile")
    boolean isValidFile(MultipartFile file);

    @Named("uploadFile")
    String uploadFile(MultipartFile file) throws IOException;

    @Named("uploadFileIfPresent")
    String uploadFileIfPresent(MultipartFile file) throws IOException;

    @Named("deleteFile")
    boolean deleteFile(String fileUrl);

    boolean fileExists(String fileUrl);
}
