package org.spacelab.housingutilitiessystemuser.models.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String id;
    private String fullName;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String email;
    private String accountNumber;

    
    private String cityName;
    private String streetName;
    private String houseNumber;
    private String apartmentNumber;
    private Double apartmentArea;

    private String photo; 
}
