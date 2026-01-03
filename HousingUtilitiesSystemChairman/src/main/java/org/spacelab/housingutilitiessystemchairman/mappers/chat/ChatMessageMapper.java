package org.spacelab.housingutilitiessystemchairman.mappers.chat;

import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.entity.chat.ChatMessage;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatMessageResponse;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class ChatMessageMapper {
    private final ChairmanRepository chairmanRepository;
    private final UserRepository userRepository;
    public ChatMessageMapper(ChairmanRepository chairmanRepository, UserRepository userRepository) {
        this.chairmanRepository = chairmanRepository;
        this.userRepository = userRepository;
    }
    public ChatMessageResponse toResponse(ChatMessage message, String currentUserId) {
        if (message == null) {
            return null;
        }
        String senderName = getSenderName(message.getSenderId(), message.getSenderType());
        String senderAvatar = getSenderAvatar(message.getSenderId(), message.getSenderType());
        boolean isOwn = currentUserId != null && currentUserId.equals(message.getSenderId());
        System.out.println("[DEBUG] Message " + message.getId() +
                " senderId='" + message.getSenderId() +
                "' currentUserId='" + currentUserId +
                "' isOwn=" + isOwn);
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation() != null ? message.getConversation().getId() : null)
                .senderId(message.getSenderId())
                .senderName(senderName)
                .senderAvatar(senderAvatar)
                .senderType(message.getSenderType())
                .content(message.getContent())
                .timestamp(message.getCreatedAt())
                .isOwn(isOwn)
                .build();
    }
    public List<ChatMessageResponse> toResponseList(List<ChatMessage> messages, String currentUserId) {
        return messages.stream()
                .map(msg -> toResponse(msg, currentUserId))
                .toList();
    }
    protected String getSenderName(String senderId, String senderType) {
        if (senderId == null || senderType == null) {
            return "Неизвестный";
        }
        if ("CHAIRMAN".equals(senderType)) {
            return chairmanRepository.findById(senderId)
                    .map(Chairman::getFullName)
                    .orElse("Председатель");
        } else if ("USER".equals(senderType)) {
            return userRepository.findById(new ObjectId(senderId))
                    .map(User::getFullName)
                    .orElse("Пользователь");
        }
        return "Неизвестный";
    }
    protected String getSenderAvatar(String senderId, String senderType) {
        if (senderId == null || senderType == null) {
            return null;
        }
        if ("CHAIRMAN".equals(senderType)) {
            return chairmanRepository.findById(senderId)
                    .map(Chairman::getPhoto)
                    .orElse(null);
        } else if ("USER".equals(senderType)) {
            return userRepository.findById(new ObjectId(senderId))
                    .map(User::getPhoto)
                    .orElse(null);
        }
        return null;
    }
}
