package org.spacelab.housingutilitiessystemadmin.controller.app;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.Admin;
import org.spacelab.housingutilitiessystemadmin.exception.OperationException;
import org.spacelab.housingutilitiessystemadmin.models.admin.ProfileResponse;
import org.spacelab.housingutilitiessystemadmin.models.admin.ProfileUpdateRequest;
import org.spacelab.housingutilitiessystemadmin.security.CustomOidcUser;
import org.spacelab.housingutilitiessystemadmin.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final AdminService adminService;


    @GetMapping
    public ModelAndView showProfilePage(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Профиль");
        model.addAttribute("pageActive", "profile");
        
        if (authentication != null && authentication.getPrincipal() instanceof CustomOidcUser customOidcUser) {
            model.addAttribute("admin", customOidcUser.getAdmin());
        }
        
        return new ModelAndView("profile");
    }

    @GetMapping("/api/current")
    @ResponseBody
    public ResponseEntity<ProfileResponse> getCurrentProfile(Authentication authentication) {
        String email = getCurrentUserEmail(authentication);
        Admin admin = adminService.findByEmail(email);
        
        if (admin == null) {
            throw new OperationException("получении профиля", 
                "Администратор не найден", HttpStatus.NOT_FOUND);
        }
        
        ProfileResponse response = mapToProfileResponse(admin);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/update")
    @ResponseBody
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @ModelAttribute ProfileUpdateRequest request,
            Authentication authentication) {
        
        String email = getCurrentUserEmail(authentication);
        log.info("Обновление профиля администратора: {}", email);
        
        Admin updatedAdmin = adminService.updateProfile(email, request);
        ProfileResponse response = mapToProfileResponse(updatedAdmin);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("avatar") MultipartFile avatarFile,
            Authentication authentication) {
        
        String email = getCurrentUserEmail(authentication);
        log.info("Загрузка аватара для администратора: {}", email);
        
        if (avatarFile.isEmpty()) {
            throw new OperationException("загрузке аватара", 
                "Файл не выбран", HttpStatus.BAD_REQUEST);
        }
        
        // Check file size (max 800KB)
        if (avatarFile.getSize() > 800 * 1024) {
            throw new OperationException("загрузке аватара", 
                "Размер файла превышает 800KB", HttpStatus.BAD_REQUEST);
        }
        
        // Check file type
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new OperationException("загрузке аватара", 
                "Допустимы только файлы изображений (JPG, PNG, GIF)", HttpStatus.BAD_REQUEST);
        }
        
        Admin updatedAdmin = adminService.updateAvatar(email, avatarFile);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Аватар успешно загружен");
        response.put("avatarPath", updatedAdmin.getPathAvatar());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteAvatar(Authentication authentication) {
        String email = getCurrentUserEmail(authentication);
        log.info("Удаление аватара для администратора: {}", email);
        
        boolean deleted = adminService.deleteAvatar(email);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", deleted ? "Аватар успешно удален" : "Аватар не найден");
        
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserEmail(Authentication authentication) {
        if (authentication == null) {
            throw new OperationException("получении профиля", 
                "Пользователь не авторизован", HttpStatus.UNAUTHORIZED);
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof CustomOidcUser customOidcUser) {
            return customOidcUser.getEmail();
        } else if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        
        throw new OperationException("получении профиля", 
            "Не удалось определить пользователя", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ProfileResponse mapToProfileResponse(Admin admin) {
        ProfileResponse response = new ProfileResponse();
        response.setId(admin.getId());
        response.setEmail(admin.getEmail());
        response.setUsername(admin.getUsername());
        response.setPathAvatar(admin.getPathAvatar());
        response.setRole(admin.getRole());
        response.setEnabled(admin.isEnabled());
        response.setPhone(admin.getPhone());
        response.setOrganization(admin.getOrganization());
        response.setAddress(admin.getAddress());
        response.setCity(admin.getCity());
        response.setState(admin.getState());
        response.setZipCode(admin.getZipCode());
        response.setCountry(admin.getCountry());
        response.setLanguage(admin.getLanguage());
        response.setTimezone(admin.getTimezone());
        response.setCurrency(admin.getCurrency());
        return response;
    }
}

