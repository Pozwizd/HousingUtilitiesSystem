package org.spacelab.housingutilitiessystemadmin.models.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.validation.ConditionalPasswordRequired;
import org.spacelab.housingutilitiessystemadmin.validation.PasswordMatches;
import org.springframework.web.multipart.MultipartFile;

@Data
@PasswordMatches
@ConditionalPasswordRequired
public class UserRequest {
    @NotBlank(message = "users.validation.firstName.NotBlank")
    @Size(min = 2, max = 50, message = "users.validation.firstName.Size")
    private String firstName;

    @Size(max = 50, message = "users.validation.middleName.Size")
    private String middleName;

    @NotBlank(message = "users.validation.lastName.NotBlank")
    @Size(min = 2, max = 50, message = "users.validation.lastName.Size")
    private String lastName;

    @NotBlank(message = "users.validation.phone.NotBlank")
    private String phone;

    @NotBlank(message = "users.validation.email.NotBlank")
    @Email(message = "users.validation.email.Email")
    private String email;

    @NotBlank(message = "users.validation.login.NotBlank")
    @Size(min = 3, max = 50, message = "users.validation.login.Size")
    private String login;

    @NotNull(message = "users.validation.cityId.NotNull")
    private ObjectId cityId;

    @NotNull(message = "users.validation.addressId.NotNull")
    private ObjectId addressId;

    @NotNull(message = "users.validation.houseId.NotNull")
    private ObjectId houseId;

    @NotBlank(message = "users.validation.houseNumber.NotBlank")
    private String houseNumber;

    @NotBlank(message = "users.validation.apartmentNumber.NotBlank")
    private String apartmentNumber;

    @NotNull(message = "users.validation.apartmentArea.NotNull")
    private Double apartmentArea;

    @NotBlank(message = "users.validation.accountNumber.NotBlank")
    private String accountNumber;

    @NotBlank(message = "users.validation.status.NotBlank")
    private String status;

    private String password;

    private String confirmPassword;

    private MultipartFile photoFile;
    private ObjectId id;

}