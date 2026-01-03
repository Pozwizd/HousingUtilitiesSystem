package org.spacelab.housingutilitiessystemadmin.models.admin;

import lombok.Data;
import org.spacelab.housingutilitiessystemadmin.entity.Role;

@Data
public class ProfileResponse {
    private String id;
    private String email;
    private String username;
    private String pathAvatar;
    private Role role;
    private boolean enabled;
    
    // Additional profile fields
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

