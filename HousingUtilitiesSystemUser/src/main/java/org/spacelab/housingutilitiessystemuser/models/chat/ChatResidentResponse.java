package org.spacelab.housingutilitiessystemuser.models.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResidentResponse {
    private String id;
    private String fullName;
    private String apartmentNumber;
    private String phone;
    private String email;
    private String photo;
}
