package org.spacelab.housingutilitiessystemuser.models.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
    private String id;
    private String fullName;
    private String contactType;
    private String role;
    private String phone;
    private String photoPath;
    private String description;
}
