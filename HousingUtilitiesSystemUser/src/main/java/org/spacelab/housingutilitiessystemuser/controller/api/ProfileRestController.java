package org.spacelab.housingutilitiessystemuser.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.models.profile.ProfileResponse;
import org.spacelab.housingutilitiessystemuser.service.FileService;
import org.spacelab.housingutilitiessystemuser.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileRestController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toProfileResponse(user));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile) {

        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        
        if (password != null && !password.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                return ResponseEntity.badRequest().build();
            }
            user.setPassword(passwordEncoder.encode(password));
        }

        
        if (fileService.isValidFile(photoFile)) {
            try {
                String photoPath = fileService.uploadFile(photoFile);
                user.setPhoto(photoPath);
            } catch (IOException e) {
                log.error("Error uploading photo", e);
                return ResponseEntity.internalServerError().build();
            }
        }

        User saved = userService.save(user);
        return ResponseEntity.ok(toProfileResponse(saved));
    }

    @PostMapping("/generate-password")
    public ResponseEntity<Map<String, String>> generatePassword() {
        String password = generateRandomPassword(12);
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        return ResponseEntity.ok(response);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            return userService.findByEmail(username)
                    .or(() -> userService.findByLogin(username))
                    .orElse(null);
        }
        return null;
    }

    private ProfileResponse toProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .accountNumber(user.getAccountNumber())
                .cityName(user.getCity() != null ? user.getCity().getName() : null)
                .streetName(user.getStreet() != null ? user.getStreet().getName() : null)
                .houseNumber(user.getHouseNumber())
                .apartmentNumber(user.getApartmentNumber())
                .apartmentArea(user.getApartmentArea())
                .photo(user.getPhoto())
                .build();
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
