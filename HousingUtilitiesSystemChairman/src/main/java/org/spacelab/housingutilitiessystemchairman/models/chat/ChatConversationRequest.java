package org.spacelab.housingutilitiessystemchairman.models.chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationRequest {
    private String userId;
    private String targetType;
}
