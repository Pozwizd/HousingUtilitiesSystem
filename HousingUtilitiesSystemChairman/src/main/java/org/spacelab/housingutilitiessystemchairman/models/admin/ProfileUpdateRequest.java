package org.spacelab.housingutilitiessystemchairman.models.admin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class ProfileUpdateRequest {
    @Size(min = 2, max = 100, message = "Username must be between 2 and 100 characters")
    private String username;
    @Email(message = "Email should be valid")
    private String email;
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    private String currentPassword;
    private MultipartFile avatarFile;
    private String phone;
    private String organization;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String language;
    private String timezone;
    private String currency;
}
