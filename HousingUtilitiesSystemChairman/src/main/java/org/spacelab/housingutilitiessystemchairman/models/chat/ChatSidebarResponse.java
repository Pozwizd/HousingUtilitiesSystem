package org.spacelab.housingutilitiessystemchairman.models.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSidebarResponse {
    private List<ChatConversationResponse> chatConversationResponses;
    private List<ChatContactResponse> chatContactResponses;
}
