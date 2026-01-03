package org.spacelab.housingutilitiessystemadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.Admin;
import org.spacelab.housingutilitiessystemadmin.exception.OperationException;
import org.spacelab.housingutilitiessystemadmin.models.admin.ProfileUpdateRequest;
import org.spacelab.housingutilitiessystemadmin.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    public Admin createAdmin(String email, String password) {
        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEnabled(true);

        return adminRepository.save(admin);
    }

    public Admin createAdmin(String email, String password, String username) {
        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEnabled(true);

        return adminRepository.save(admin);
    }

    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    public Optional<Admin> findOptByEmail(String email) {
        return adminRepository.findOptByEmail(email);
    }
    
    public Optional<Admin> findById(String id) {
        return adminRepository.findById(id);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    public Admin updateProfile(String email, ProfileUpdateRequest request) {
        Admin admin = findByEmail(email);
        if (admin == null) {
            throw new OperationException("обновлении профиля", 
                "Администратор с email " + email + " не найден", HttpStatus.NOT_FOUND);
        }
        
        // Update basic fields
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            admin.setUsername(request.getUsername());
        }
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
                throw new OperationException("обновлении профиля", 
                    "Необходимо указать текущий пароль для изменения", HttpStatus.BAD_REQUEST);
            }
            if (!checkPassword(request.getCurrentPassword(), admin.getPassword())) {
                throw new OperationException("обновлении профиля", 
                    "Неверный текущий пароль", HttpStatus.BAD_REQUEST);
            }
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Update email if changed
        if (request.getEmail() != null && !request.getEmail().isEmpty() 
            && !request.getEmail().equals(admin.getEmail())) {
            // Check if new email is already taken
            Admin existingAdmin = findByEmail(request.getEmail());
            if (existingAdmin != null && !existingAdmin.getId().equals(admin.getId())) {
                throw new OperationException("обновлении профиля", 
                    "Email " + request.getEmail() + " уже используется", HttpStatus.CONFLICT);
            }
            admin.setEmail(request.getEmail());
        }
        
        // Update additional profile fields
        if (request.getPhone() != null) {
            admin.setPhone(request.getPhone());
        }
        if (request.getOrganization() != null) {
            admin.setOrganization(request.getOrganization());
        }
        if (request.getAddress() != null) {
            admin.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            admin.setCity(request.getCity());
        }
        if (request.getState() != null) {
            admin.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            admin.setZipCode(request.getZipCode());
        }
        if (request.getCountry() != null) {
            admin.setCountry(request.getCountry());
        }
        if (request.getLanguage() != null) {
            admin.setLanguage(request.getLanguage());
        }
        if (request.getTimezone() != null) {
            admin.setTimezone(request.getTimezone());
        }
        if (request.getCurrency() != null) {
            admin.setCurrency(request.getCurrency());
        }
        
        Admin updatedAdmin = adminRepository.save(admin);
        log.info("Профиль администратора {} успешно обновлен", email);
        return updatedAdmin;
    }
    
    public Admin updateAvatar(String email, MultipartFile avatarFile) {
        Admin admin = findByEmail(email);
        if (admin == null) {
            throw new OperationException("обновлении аватара", 
                "Администратор с email " + email + " не найден", HttpStatus.NOT_FOUND);
        }
        
        try {
            // Delete old avatar if exists
            if (admin.getPathAvatar() != null && !admin.getPathAvatar().isEmpty()) {
                fileService.deleteFile(admin.getPathAvatar());
            }
            
            // Upload new avatar
            String avatarPath = fileService.uploadFile(avatarFile);
            admin.setPathAvatar(avatarPath);
            
            Admin updatedAdmin = adminRepository.save(admin);
            log.info("Аватар администратора {} успешно обновлен", email);
            return updatedAdmin;
        } catch (Exception e) {
            log.error("Ошибка при обновлении аватара администратора {}", email, e);
            throw new OperationException("обновлении аватара", 
                "Ошибка при загрузке файла: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    public boolean deleteAvatar(String email) {
        Admin admin = findByEmail(email);
        if (admin == null) {
            throw new OperationException("удалении аватара", 
                "Администратор с email " + email + " не найден", HttpStatus.NOT_FOUND);
        }
        
        try {
            if (admin.getPathAvatar() != null && !admin.getPathAvatar().isEmpty()) {
                fileService.deleteFile(admin.getPathAvatar());
                admin.setPathAvatar(null);
                adminRepository.save(admin);
                log.info("Аватар администратора {} успешно удален", email);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Ошибка при удалении аватара администратора {}", email, e);
            throw new OperationException("удалении аватара", 
                "Ошибка при удалении файла: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
