package org.spacelab.housingutilitiessystemuser.entity.chat;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ChatParticipant {

    
    private String id;

    
    private String type;

    
    private String fullName;

    
    private String avatar;

    
    public static final String TYPE_CHAIRMAN = "CHAIRMAN";
    public static final String TYPE_USER = "USER";

    public boolean isChairman() {
        return TYPE_CHAIRMAN.equals(type);
    }

    public boolean isUser() {
        return TYPE_USER.equals(type);
    }
}
