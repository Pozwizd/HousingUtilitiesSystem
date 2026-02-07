package org.spacelab.housingutilitiessystemuser.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.Chairman;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.entity.chat.ChatMessage;
import org.spacelab.housingutilitiessystemuser.entity.chat.Conversation;
import org.spacelab.housingutilitiessystemuser.entity.location.Status;
import org.spacelab.housingutilitiessystemuser.exception.OperationException;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatContactResponse;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatConversationResponse;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatMessageResponse;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatSidebarResponse;
import org.spacelab.housingutilitiessystemuser.models.chat.event.ChatEvent;
import org.spacelab.housingutilitiessystemuser.repository.ChairmanRepository;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.spacelab.housingutilitiessystemuser.repository.chat.ChatMessageRepository;
import org.spacelab.housingutilitiessystemuser.repository.chat.ConversationRepository;
import org.spacelab.housingutilitiessystemuser.service.chat.ChatEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

        private final ConversationRepository conversationRepository;
        private final ChatMessageRepository chatMessageRepository;
        private final UserRepository userRepository;
        private final ChairmanRepository chairmanRepository;
        private final ChatEventPublisher chatEventPublisher;

        @Autowired
        @Lazy
        private AiService aiService;

        public User getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new OperationException("работе с чатом", "Аутентификация не выполнена",
                                        HttpStatus.UNAUTHORIZED);
                }

                String login = authentication.getName();
                return userRepository.findByEmail(login)
                                .orElseThrow(() -> new OperationException("работе с чатом",
                                                "Пользователь " + login + " не найден", HttpStatus.UNAUTHORIZED));
        }

        public ChatSidebarResponse getChatSidebar() {
                User user = getCurrentUser();
                log.debug("Building chat sidebar for user: {}", user.getId());

                Set<Conversation> conversations = user.getConversations();
                if (conversations == null) {
                        conversations = new HashSet<>();
                }
                log.debug("Found {} conversations for user", conversations.size());

                List<ChatConversationResponse> conversationResponses = conversations.stream()
                                .map(conv -> toConversationResponse(conv, user.getId()))
                                .sorted((c1, c2) -> {

                                        String t1 = c1.getLastMessageTime();
                                        String t2 = c2.getLastMessageTime();
                                        if (t1 == null || t1.isEmpty())
                                                return 1;
                                        if (t2 == null || t2.isEmpty())
                                                return -1;
                                        return t2.compareTo(t1);
                                })
                                .collect(Collectors.toList());

                Set<String> participantsInConversations = new HashSet<>();
                for (Conversation conv : conversations) {
                        String otherId = getOtherParticipantId(conv, user.getId());
                        if (otherId != null) {
                                participantsInConversations.add(otherId);
                        }
                }

                ChatContactResponse chairman = null;
                if (user.getHouse() != null) {
                        chairman = chairmanRepository.findByHouseId(user.getHouse().getId())
                                        .filter(ch -> !participantsInConversations.contains(ch.getId()))
                                        .map(ch -> ChatContactResponse.builder()
                                                        .id(ch.getId())
                                                        .name(ch.getFullName())
                                                        .avatar(ch.getPhoto())
                                                        .isOnline(ch.isOnline())
                                                        .participantType("CHAIRMAN")
                                                        .build())
                                        .orElse(null);
                }

                List<ChatContactResponse> contacts = new ArrayList<>();
                if (user.getHouse() != null) {
                        contacts = userRepository.findByHouse(user.getHouse()).stream()
                                        .filter(u -> !u.getId().equals(user.getId()))
                                        .filter(u -> Status.ACTIVE.equals(u.getStatus()))
                                        .filter(u -> !participantsInConversations.contains(u.getId()))
                                        .map(u -> ChatContactResponse.builder()
                                                        .id(u.getId())
                                                        .name(u.getFullName())
                                                        .avatar(u.getPhoto())
                                                        .isOnline(u.isOnline())
                                                        .participantType("USER")
                                                        .build())
                                        .collect(Collectors.toList());
                }

                // Build AI assistant contact (always visible)
                ChatContactResponse aiAssistant = buildAiAssistantContact(participantsInConversations);

                return ChatSidebarResponse.builder()
                                .chatConversationResponses(conversationResponses)
                                .chairman(chairman)
                                .aiAssistant(aiAssistant)
                                .chatContactResponses(contacts)
                                .build();
        }

        public ChatMessageResponse sendMessage(String conversationId, String content, User sender) {
                log.debug("Sending message to conversation {}: {}", conversationId, content);

                Conversation conversation = conversationRepository.findById(conversationId)
                                .orElseThrow(() -> new OperationException("отправке сообщения",
                                                "Диалог не найден: " + conversationId, HttpStatus.NOT_FOUND));

                if (sender.getConversations() == null ||
                                !sender.getConversations().stream().anyMatch(c -> c.getId().equals(conversationId))) {
                        throw new OperationException("отправке сообщения",
                                        "Нет доступа к диалогу", HttpStatus.FORBIDDEN);
                }

                ChatMessage message = ChatMessage.builder()
                                .conversation(conversation)
                                .content(content)
                                .senderId(sender.getId())
                                .senderType("USER")
                                .createdAt(Instant.now())
                                .build();

                ChatMessage savedMessage = chatMessageRepository.save(message);
                log.info("✅ Message saved: {}", savedMessage.getId());

                conversation.setUpdatedAt(Instant.now());
                conversationRepository.save(conversation);

                String recipientId = getOtherParticipantId(conversation, sender.getId());
                String recipientType = getOtherParticipantType(conversation, sender.getId());

                if (recipientId != null && "CHAIRMAN".equals(recipientType)) {
                        chairmanRepository.findById(recipientId).ifPresent(chairman -> {
                                if (chairman.isOnline()) {
                                        log.info("📨 Recipient {} is online, publishing event via Redis",
                                                        chairman.getLogin());
                                        ChatEvent event = buildChatEventForChairman(savedMessage, chairman, sender);
                                        chatEventPublisher.publishMessageEvent(event);
                                }
                        });
                } else if (recipientId != null && "USER".equals(recipientType)) {
                        userRepository.findById(recipientId).ifPresent(targetUser -> {
                                if (targetUser.isOnline()) {
                                        log.info("📨 Recipient {} is online, publishing event via Redis",
                                                        targetUser.getEmail());
                                        ChatEvent event = buildChatEventForUser(savedMessage, targetUser, sender);
                                        chatEventPublisher.publishMessageEvent(event);
                                }
                        });
                } else if (AiService.isAiParticipant(recipientId)) {
                        // Handle AI recipient - get AI response asynchronously
                        log.info("🤖 Message to AI, triggering async response");
                        triggerAiResponse(conversation, content, sender);
                }

                return toMessageResponse(savedMessage, sender.getId());
        }

        public Conversation getOrCreateConversation(String targetId, String targetType) {
                User currentUser = getCurrentUser();

                Set<Conversation> myConversations = currentUser.getConversations();
                if (myConversations != null) {
                        for (Conversation conv : myConversations) {
                                String otherId = getOtherParticipantId(conv, currentUser.getId());
                                if (targetId.equals(otherId)) {
                                        log.debug("Found existing conversation with {}", targetId);
                                        return conv;
                                }
                        }
                }

                log.info("Creating new conversation between user {} and {} {}",
                                currentUser.getId(), targetType, targetId);

                Conversation newConversation = Conversation.builder()
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
                Conversation savedConversation = conversationRepository.save(newConversation);

                if (currentUser.getConversations() == null) {
                        currentUser.setConversations(new HashSet<>());
                }
                currentUser.getConversations().add(savedConversation);
                userRepository.save(currentUser);

                if ("CHAIRMAN".equals(targetType)) {
                        Chairman target = chairmanRepository.findById(targetId)
                                        .orElseThrow(() -> new OperationException("создании диалога",
                                                        "Председатель не найден: " + targetId, HttpStatus.NOT_FOUND));
                        if (target.getConversations() == null) {
                                target.setConversations(new HashSet<>());
                        }
                        target.getConversations().add(savedConversation);
                        chairmanRepository.save(target);
                } else if (AiService.AI_PARTICIPANT_TYPE.equals(targetType)) {
                        // AI doesn't need DB association, just log
                        log.info("🤖 Created conversation with AI assistant");
                } else {
                        User target = userRepository.findById(targetId)
                                        .orElseThrow(() -> new OperationException("создании диалога",
                                                        "Пользователь не найден: " + targetId, HttpStatus.NOT_FOUND));
                        if (target.getConversations() == null) {
                                target.setConversations(new HashSet<>());
                        }
                        target.getConversations().add(savedConversation);
                        userRepository.save(target);
                }

                ChatMessage initMessage = ChatMessage.builder()
                                .conversation(savedConversation)
                                .content("")
                                .senderId(targetId)
                                .senderType(targetType)
                                .createdAt(Instant.now())
                                .build();
                chatMessageRepository.save(initMessage);

                return savedConversation;
        }

        public List<ChatMessageResponse> getConversationMessages(String conversationId, int limit) {
                log.info("📥 Loading messages for conversation: {}, limit: {}", conversationId, limit);

                User user = getCurrentUser();

                if (user.getConversations() == null ||
                                !user.getConversations().stream().anyMatch(c -> c.getId().equals(conversationId))) {
                        throw new OperationException("загрузке сообщений",
                                        "Нет доступа к диалогу", HttpStatus.FORBIDDEN);
                }

                List<ChatMessage> messages = chatMessageRepository.findByConversationId(
                                conversationId, PageRequest.of(0, limit));

                log.info("📨 Found {} messages for conversation {}", messages.size(), conversationId);

                Collections.reverse(messages);

                return messages.stream()
                                .filter(msg -> msg.getContent() != null && !msg.getContent().isEmpty())
                                .map(msg -> toMessageResponse(msg, user.getId()))
                                .collect(Collectors.toList());
        }

        public void clearConversationMessages(String conversationId) {
                log.info("🗑️ Deleting conversation: {}", conversationId);

                User user = getCurrentUser();

                Conversation conversation = conversationRepository.findById(conversationId)
                                .orElseThrow(() -> new OperationException("удалении чата",
                                                "Диалог не найден: " + conversationId, HttpStatus.NOT_FOUND));

                if (user.getConversations() == null ||
                                !user.getConversations().stream().anyMatch(c -> c.getId().equals(conversationId))) {
                        throw new OperationException("удалении чата",
                                        "Нет доступа к диалогу", HttpStatus.FORBIDDEN);
                }

                // Delete all messages in this conversation
                chatMessageRepository.deleteByConversationId(conversationId);

                // Remove conversation from user's list
                user.getConversations().removeIf(c -> c.getId().equals(conversationId));
                userRepository.save(user);

                // Delete the conversation entity
                conversationRepository.delete(conversation);

                log.info("✅ Deleted conversation: {}", conversationId);
        }

        public ChatConversationResponse getConversationInfo(String conversationId) {
                User user = getCurrentUser();

                Conversation conversation = conversationRepository.findById(conversationId)
                                .orElseThrow(() -> new OperationException("получении диалога",
                                                "Диалог не найден: " + conversationId, HttpStatus.NOT_FOUND));

                if (user.getConversations() == null ||
                                !user.getConversations().stream().anyMatch(c -> c.getId().equals(conversationId))) {
                        throw new OperationException("получении диалога",
                                        "Нет доступа к диалогу", HttpStatus.FORBIDDEN);
                }

                return toConversationResponse(conversation, user.getId());
        }

        private String getOtherParticipantId(Conversation conversation, String currentUserId) {
                List<ChatMessage> messages = chatMessageRepository.findLatestByConversationId(conversation.getId());
                return messages.stream()
                                .map(ChatMessage::getSenderId)
                                .filter(id -> !id.equals(currentUserId))
                                .findFirst()
                                .orElse(null);
        }

        private String getOtherParticipantType(Conversation conversation, String currentUserId) {
                List<ChatMessage> messages = chatMessageRepository.findLatestByConversationId(conversation.getId());
                return messages.stream()
                                .filter(msg -> !msg.getSenderId().equals(currentUserId))
                                .map(ChatMessage::getSenderType)
                                .findFirst()
                                .orElse(null);
        }

        private ChatConversationResponse toConversationResponse(Conversation conversation, String currentUserId) {
                List<ChatMessage> lastMessages = chatMessageRepository.findLatestByConversationId(conversation.getId());
                ChatMessage lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0);

                String otherId = getOtherParticipantId(conversation, currentUserId);
                String otherType = getOtherParticipantType(conversation, currentUserId);

                String name = "Новый диалог";
                String avatar = null;
                boolean online = false;
                String participantType = otherType;

                if (otherId != null && "CHAIRMAN".equals(otherType)) {
                        Chairman ch = chairmanRepository.findById(otherId).orElse(null);
                        if (ch != null) {
                                name = ch.getFullName();
                                avatar = ch.getPhoto();
                                online = ch.isOnline();
                        }
                } else if (otherId != null && "USER".equals(otherType)) {
                        User u = userRepository.findById(otherId).orElse(null);
                        if (u != null) {
                                name = u.getFullName();
                                avatar = u.getPhoto();
                                online = u.isOnline();
                        }
                } else if (AiService.isAiParticipant(otherId)) {
                        name = AiService.AI_NAME;
                        avatar = AiService.AI_AVATAR;
                        online = true; // AI is always online
                }

                return ChatConversationResponse.builder()
                                .id(conversation.getId())
                                .participantId(otherId)
                                .name(name)
                                .avatar(avatar)
                                .lastMessage(lastMessage != null ? lastMessage.getContent() : "")
                                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt().toString() : "")
                                .isOnline(online)
                                .participantType(participantType)
                                .build();
        }

        private ChatMessageResponse toMessageResponse(ChatMessage message, String currentUserId) {
                String senderName;
                String senderAvatar;

                if ("CHAIRMAN".equals(message.getSenderType())) {
                        Chairman chairman = chairmanRepository.findById(message.getSenderId()).orElse(null);
                        senderName = chairman != null ? chairman.getFullName() : "Председатель";
                        senderAvatar = chairman != null ? chairman.getPhoto() : null;
                } else if (AiService.AI_PARTICIPANT_TYPE.equals(message.getSenderType())) {
                        senderName = AiService.AI_NAME;
                        senderAvatar = AiService.AI_AVATAR;
                } else {
                        User user = userRepository.findById(message.getSenderId()).orElse(null);
                        senderName = user != null ? user.getFullName() : "Пользователь";
                        senderAvatar = user != null ? user.getPhoto() : null;
                }

                boolean isOwn = message.getSenderId().equals(currentUserId);

                return ChatMessageResponse.builder()
                                .id(message.getId())
                                .conversationId(message.getConversation().getId())
                                .senderId(message.getSenderId())
                                .senderName(senderName)
                                .senderAvatar(senderAvatar)
                                .senderType(message.getSenderType())
                                .content(message.getContent())
                                .timestamp(message.getCreatedAt())
                                .isOwn(isOwn)
                                .build();
        }

        private ChatEvent buildChatEventForChairman(ChatMessage message, Chairman recipient, User sender) {
                return ChatEvent.builder()
                                .eventType("MESSAGE_SENT")
                                .targetUserId(recipient.getId())
                                .targetUserRole("CHAIRMAN")
                                .conversationId(message.getConversation().getId())
                                .messageId(message.getId())
                                .senderId(sender.getId())
                                .senderName(sender.getFullName())
                                .senderAvatar(sender.getPhoto())
                                .senderType("USER")
                                .lastMessage(message.getContent())
                                .timestamp(message.getCreatedAt())
                                .isOnline(sender.isOnline())
                                .build();
        }

        private ChatEvent buildChatEventForUser(ChatMessage message, User recipient, User sender) {
                return ChatEvent.builder()
                                .eventType("MESSAGE_SENT")
                                .targetUserId(recipient.getId())
                                .targetUserRole("USER")
                                .conversationId(message.getConversation().getId())
                                .messageId(message.getId())
                                .senderId(sender.getId())
                                .senderName(sender.getFullName())
                                .senderAvatar(sender.getPhoto())
                                .senderType("USER")
                                .lastMessage(message.getContent())
                                .timestamp(message.getCreatedAt())
                                .isOnline(sender.isOnline())
                                .build();
        }

        /**
         * Build AI assistant contact for sidebar.
         * Returns null if user already has an active conversation with AI.
         */
        private ChatContactResponse buildAiAssistantContact(Set<String> participantsInConversations) {
                if (participantsInConversations.contains(AiService.AI_PARTICIPANT_ID)) {
                        return null; // Already has conversation with AI
                }
                return ChatContactResponse.builder()
                                .id(AiService.AI_PARTICIPANT_ID)
                                .name(AiService.AI_NAME)
                                .avatar(AiService.AI_AVATAR)
                                .isOnline(true) // AI is always online
                                .participantType(AiService.AI_PARTICIPANT_TYPE)
                                .build();
        }

        /**
         * Trigger AI response asynchronously and save it as a message.
         * Now includes conversation history and automatic context summarization.
         */
        private void triggerAiResponse(Conversation conversation, String userMessage, User sender) {
                CompletableFuture.runAsync(() -> {
                        try {
                                log.info("🤖 Processing AI request with history for conversation {}",
                                                conversation.getId());

                                // Load last 10 messages for context
                                List<ChatMessage> recentMessages = chatMessageRepository.findByConversationId(
                                                conversation.getId(), PageRequest.of(0, 10));
                                Collections.reverse(recentMessages); // Oldest first

                                // Build messages array for AI
                                List<Map<String, String>> aiMessages = new java.util.ArrayList<>();
                                for (ChatMessage msg : recentMessages) {
                                        if (msg.getContent() == null || msg.getContent().isEmpty())
                                                continue;
                                        String role = AiService.AI_PARTICIPANT_TYPE.equals(msg.getSenderType())
                                                        ? "assistant"
                                                        : "user";
                                        aiMessages.add(Map.of("role", role, "content", msg.getContent()));
                                }

                                // Check if we need to summarize (every 5 user messages)
                                int messageCount = conversation.getMessagesSinceLastSummary() + 1;
                                String contextSummary = conversation.getContextSummary();

                                if (messageCount >= 5) {
                                        log.info("🤖 Triggering context summarization (message count: {})",
                                                        messageCount);
                                        String newSummary = aiService.summarizeContext(aiMessages);
                                        if (newSummary != null) {
                                                // Combine old and new summaries
                                                if (contextSummary != null && !contextSummary.isEmpty()) {
                                                        contextSummary = contextSummary + "\n" + newSummary;
                                                } else {
                                                        contextSummary = newSummary;
                                                }
                                                conversation.setContextSummary(contextSummary);
                                                conversation.setMessagesSinceLastSummary(0);
                                                log.info("🤖 Context summarized and saved");
                                        }
                                } else {
                                        conversation.setMessagesSinceLastSummary(messageCount);
                                }

                                // Get AI response with history
                                String aiResponse = aiService.getAiResponseWithHistory(aiMessages, contextSummary);
                                log.info("🤖 AI response received, saving to conversation");

                                // Save AI response as a message
                                ChatMessage aiMessage = ChatMessage.builder()
                                                .conversation(conversation)
                                                .content(aiResponse)
                                                .senderId(AiService.AI_PARTICIPANT_ID)
                                                .senderType(AiService.AI_PARTICIPANT_TYPE)
                                                .createdAt(Instant.now())
                                                .build();
                                ChatMessage savedAiMessage = chatMessageRepository.save(aiMessage);

                                // Update conversation timestamp
                                conversation.setUpdatedAt(Instant.now());
                                conversationRepository.save(conversation);

                                // Publish event to user via WebSocket
                                ChatEvent event = ChatEvent.builder()
                                                .eventType("MESSAGE_SENT")
                                                .targetUserId(sender.getId())
                                                .targetUserRole("USER")
                                                .conversationId(conversation.getId())
                                                .messageId(savedAiMessage.getId())
                                                .senderId(AiService.AI_PARTICIPANT_ID)
                                                .senderName(AiService.AI_NAME)
                                                .senderAvatar(AiService.AI_AVATAR)
                                                .senderType(AiService.AI_PARTICIPANT_TYPE)
                                                .lastMessage(aiResponse)
                                                .timestamp(savedAiMessage.getCreatedAt())
                                                .isOnline(true)
                                                .build();
                                chatEventPublisher.publishMessageEvent(event);

                                log.info("✅ AI response published to user {}", sender.getId());
                        } catch (Exception ex) {
                                log.error("❌ Failed to get AI response: {}", ex.getMessage(), ex);
                        }
                });
        }
}
