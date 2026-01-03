package org.spacelab.housingutilitiessystemchairman.models.chat.command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationDirective {
    String id;
    String name;
    String avatar;
    String lastMessage;
    String lastMessageTime;
    boolean isOnline;
}
