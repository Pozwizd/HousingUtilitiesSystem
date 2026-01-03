package org.spacelab.housingutilitiessystemuser.models.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatContactResponse {
    private String id;
    private String name;
    private String avatar;
    private boolean isOnline;
    private String participantType; 
}
