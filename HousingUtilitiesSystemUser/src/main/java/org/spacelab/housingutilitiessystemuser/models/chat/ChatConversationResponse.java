package org.spacelab.housingutilitiessystemuser.models.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationResponse {
    String id;
    String participantId; 
    String name; 
    String avatar;
    String lastMessage;
    String lastMessageTime;
    boolean isOnline;
    String participantType; 
}
