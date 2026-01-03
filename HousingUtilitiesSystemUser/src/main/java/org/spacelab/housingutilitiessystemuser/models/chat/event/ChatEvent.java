package org.spacelab.housingutilitiessystemuser.models.chat.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEvent implements Serializable {

    
    private String eventType;

    
    private String targetUserId;

    
    private String targetUserRole;

    
    private String conversationId;

    
    private String messageId;

    
    private String senderId;

    
    private String senderName;

    
    private String senderAvatar;

    
    private String senderType;

    
    private String lastMessage;

    
    private Instant timestamp;

    
    private boolean isOnline;

    
    private int unreadCount;
}
