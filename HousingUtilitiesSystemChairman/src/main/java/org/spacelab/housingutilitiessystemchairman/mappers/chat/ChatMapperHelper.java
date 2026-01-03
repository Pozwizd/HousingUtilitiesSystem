package org.spacelab.housingutilitiessystemchairman.mappers.chat;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.entity.chat.ChatMessage;
import org.spacelab.housingutilitiessystemchairman.entity.chat.Conversation;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.chat.ChatMessageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@RequiredArgsConstructor
public class ChatMapperHelper {
    private final ChatMessageRepository chatMessageRepository;
    @Named("getUserName")
    public String getUserName(User user) {
        return user != null ? user.getFullName() : "Unknown";
    }
    @Named("getUserAvatar")
    public String getUserAvatar(User user) {
        return user != null ? user.getPhoto() : null;
    }
    @Named("isUserOnline")
    public boolean isUserOnline(User user) {
        return user != null && user.isOnline();
    }
    @Named("getLastMessageContent")
    public String getLastMessageContent(Conversation conversation) {
        List<ChatMessage> messages = chatMessageRepository.findLatestByConversationId(conversation.getId());
        ChatMessage lastMsg = messages.isEmpty() ? null : messages.get(0);
        return lastMsg != null ? lastMsg.getContent() : "";
    }
    @Named("getLastMessageTime")
    public String getLastMessageTime(Conversation conversation) {
        List<ChatMessage> messages = chatMessageRepository.findLatestByConversationId(conversation.getId());
        ChatMessage lastMsg = messages.isEmpty() ? null : messages.get(0);
        return lastMsg != null ? lastMsg.getCreatedAt().toString() : "";
    }
}
