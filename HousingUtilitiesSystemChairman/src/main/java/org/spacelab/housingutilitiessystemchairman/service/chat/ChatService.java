package org.spacelab.housingutilitiessystemchairman.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.entity.chat.ChatMessage;
import org.spacelab.housingutilitiessystemchairman.entity.chat.Conversation;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.entity.location.Status;
import org.spacelab.housingutilitiessystemchairman.exception.OperationException;
import org.spacelab.housingutilitiessystemchairman.mappers.chat.ChatMessageMapper;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatContactResponse;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatConversationResponse;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatMessageResponse;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatSidebarResponse;
import org.spacelab.housingutilitiessystemchairman.models.chat.event.ChatEvent;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.HouseRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.UserRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.chat.ChatMessageRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.chat.ConversationRepository;
import org.spacelab.housingutilitiessystemchairman.service.ChairmanService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
        private final ChairmanService chairmanService;
        private final ChairmanRepository chairmanRepository;
        private final ConversationRepository conversationRepository;
        private final ChatMessageRepository chatMessageRepository;
        private final HouseRepository houseRepository;
        private final UserRepository userRepository;
        private final ChatMessageMapper chatMessageMapper;
        private final ChatEventPublisher chatEventPublisher;
        public Chairman getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new OperationException("работе с чатом", "Аутентификация не выполнена",
                                        HttpStatus.UNAUTHORIZED);
                }
                String login = authentication.getName();
                return chairmanService.findByEmail(login)
                                .or(() -> chairmanService.findByLogin(login))
                                .orElseThrow(() -> new OperationException("работе с чатом",
                                                "Пользователь " + login + " не найден", HttpStatus.UNAUTHORIZED));
        }
        public ChatSidebarResponse getChatSidebar() {
                Chairman chairman = getCurrentUser();
                log.debug("Building chat sidebar for chairman: {}", chairman.getId());
                Set<Conversation> conversations = chairman.getConversations();
                if (conversations == null) {
                        conversations = new HashSet<>();
                }
                log.debug("Found {} conversations for chairman", conversations.size());
            List<ObjectId> conversationIds = conversations.stream()
                    .map(c -> new ObjectId(c.getId()))
                    .collect(Collectors.toList());
            Map<String, ChatMessage> lastMessageByConversation = new HashMap<>();
            if (!conversationIds.isEmpty()) {
                List<ChatMessage> allMessages = chatMessageRepository.findByConversationIdIn(conversationIds);
                for (ChatMessage msg : allMessages) {
                    String convId = msg.getConversation().getId();
                    if (!lastMessageByConversation.containsKey(convId)) {
                        lastMessageByConversation.put(convId, msg);
                    }
                }
            }
            Set<String> userParticipantIds = new HashSet<>();
            Set<String> chairmanParticipantIds = new HashSet<>();
            Map<String, ParticipantInfo> participantInfoByConversation = new HashMap<>();
            for (ChatMessage msg : lastMessageByConversation.values()) {
                if (!msg.getSenderId().equals(chairman.getId())) {
                    String convId = msg.getConversation().getId();
                    participantInfoByConversation.put(convId,
                            new ParticipantInfo(msg.getSenderId(), msg.getSenderType()));
                    if ("USER".equals(msg.getSenderType())) {
                        userParticipantIds.add(msg.getSenderId());
                    } else if ("CHAIRMAN".equals(msg.getSenderType())) {
                        chairmanParticipantIds.add(msg.getSenderId());
                    }
                }
            }
            for (Conversation conv : conversations) {
                if (!participantInfoByConversation.containsKey(conv.getId())) {
                    List<ChatMessage> msgs = chatMessageRepository.findLatestByConversationId(conv.getId());
                    for (ChatMessage msg : msgs) {
                        if (!msg.getSenderId().equals(chairman.getId())) {
                            participantInfoByConversation.put(conv.getId(),
                                    new ParticipantInfo(msg.getSenderId(),
                                            msg.getSenderType()));
                            if ("USER".equals(msg.getSenderType())) {
                                userParticipantIds.add(msg.getSenderId());
                            } else if ("CHAIRMAN".equals(msg.getSenderType())) {
                                chairmanParticipantIds.add(msg.getSenderId());
                            }
                            break;
                        }
                    }
                }
            }
            Map<String, User> usersById = new HashMap<>();
            if (!userParticipantIds.isEmpty()) {
                List<ObjectId> userIds = userParticipantIds.stream()
                        .map(ObjectId::new)
                        .collect(Collectors.toList());
                userRepository.findAllById(userIds).forEach(u -> usersById.put(u.getId(), u));
            }
            Map<String, Chairman> chairmansById = new HashMap<>();
            if (!chairmanParticipantIds.isEmpty()) {
                chairmanRepository.findAllById(chairmanParticipantIds)
                        .forEach(c -> chairmansById.put(c.getId(), c));
            }
                List<ChatConversationResponse> conversationResponses = conversations.stream()
                        .map(conv -> toConversationResponseOptimized(conv, chairman.getId(),
                                lastMessageByConversation.get(conv.getId()),
                                participantInfoByConversation.get(conv.getId()),
                                usersById, chairmansById))
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
            Set<String> participantsInConversations = participantInfoByConversation.values().stream()
                    .map(ParticipantInfo::id)
                    .collect(Collectors.toSet());
                Set<House> chairmanHouses = houseRepository.findByChairman(chairman);
            List<ChatContactResponse> contacts;
            if (chairmanHouses.isEmpty()) {
                contacts = new ArrayList<>();
            } else {
                contacts = userRepository.findByHouseIn(new ArrayList<>(chairmanHouses)).stream()
                        .distinct()
                        .filter(user -> user.getStatus() == Status.ACTIVE)
                        .filter(user -> !participantsInConversations.contains(user.getId()))
                        .sorted((u1, u2) -> Boolean.compare(u2.isOnline(), u1.isOnline()))
                        .map(u -> ChatContactResponse.builder()
                                .id(u.getId())
                                .name(u.getFullName())
                                .avatar(u.getPhoto())
                                .online(u.isOnline())
                                .participantType("USER")
                                .build())
                        .collect(Collectors.toList());
            }
                return ChatSidebarResponse.builder()
                                .chatConversationResponses(conversationResponses)
                                .chatContactResponses(contacts)
                                .build();
        }

    private ChatConversationResponse toConversationResponseOptimized(
            Conversation conversation,
            String currentUserId,
            ChatMessage lastMessage,
            ParticipantInfo participantInfo,
            Map<String, User> usersById,
            Map<String, Chairman> chairmansById) {
        String name = "Новый диалог";
        String avatar = null;
        boolean online = false;
        String participantId = null;
        String participantType = null;
        if (participantInfo != null) {
            participantId = participantInfo.id();
            participantType = participantInfo.type();
            if ("USER".equals(participantType) && usersById.containsKey(participantId)) {
                User u = usersById.get(participantId);
                name = u.getFullName();
                avatar = u.getPhoto();
                online = u.isOnline();
            } else if ("CHAIRMAN".equals(participantType) && chairmansById.containsKey(participantId)) {
                Chairman ch = chairmansById.get(participantId);
                name = ch.getFullName();
                avatar = ch.getPhoto();
                online = ch.isOnline();
            }
        }
        return ChatConversationResponse.builder()
                .id(conversation.getId())
                .participantId(participantId)
                .name(name)
                .avatar(avatar)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : "")
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt().toString() : "")
                .online(online)
                .participantType(participantType)
                .build();
    }

    private record ParticipantInfo(String id, String type) {
    }
        public ChatMessageResponse sendMessage(String conversationId, String content, Chairman sender) {
                log.debug("Sending message to conversation {}: {}", conversationId, content);
                Conversation conversation = conversationRepository.findById(conversationId)
                                .orElseThrow(() -> new OperationException("отправке сообщения",
                                                "Диалог не найден: " + conversationId, HttpStatus.NOT_FOUND));
                if (sender.getConversations() == null ||
                                !sender.getConversations().stream().anyMatch(c -> c.getId().equals(conversationId))) {
                        throw new OperationException("отправке сообщения",
                                        "Нет доступа к диалогу", HttpStatus.FORBIDDEN);
                }
                ChatMessage message = new ChatMessage();
                message.setConversation(conversation);
                message.setContent(content);
                message.setSenderId(sender.getId());
                message.setSenderType("CHAIRMAN");
                message.setCreatedAt(Instant.now());
                ChatMessage savedMessage = chatMessageRepository.save(message);
                log.info("✅ Message saved: {}", savedMessage.getId());
                conversation.setUpdatedAt(Instant.now());
                conversationRepository.save(conversation);
                String recipientId = getOtherParticipantId(conversation, sender.getId());
                String recipientType = getOtherParticipantType(conversation, sender.getId());
                if (recipientId != null && "USER".equals(recipientType)) {
                        userRepository.findById(new ObjectId(recipientId)).ifPresent(user -> {
                                if (user.isOnline()) {
                                        log.info("📨 Recipient {} is online, publishing event via Redis",
                                                        user.getLogin());
                                        ChatEvent event = buildChatEventForUser(savedMessage, user, sender);
                                        chatEventPublisher.publishMessageEvent(event);
                                }
                        });
                } else if (recipientId != null && "CHAIRMAN".equals(recipientType)) {
                        chairmanRepository.findById(recipientId).ifPresent(targetChairman -> {
                                if (targetChairman.isOnline()) {
                                        log.info("📨 Recipient {} is online, publishing event via Redis",
                                                        targetChairman.getLogin());
                                        ChatEvent event = buildChatEventForChairman(savedMessage, targetChairman,
                                                        sender);
                                        chatEventPublisher.publishMessageEvent(event);
                                }
                        });
                }
                return chatMessageMapper.toResponse(savedMessage, sender.getId());
        }
        public Conversation getOrCreateConversation(String targetId, String targetType) {
                Chairman chairman = getCurrentUser();
                Set<Conversation> myConversations = chairman.getConversations();
                if (myConversations != null) {
                        for (Conversation conv : myConversations) {
                                String otherId = getOtherParticipantId(conv, chairman.getId());
                                if (targetId.equals(otherId)) {
                                        log.debug("Found existing conversation with {}", targetId);
                                        return conv;
                                }
                        }
                }
                log.info("Creating new conversation between chairman {} and {} {}",
                                chairman.getId(), targetType, targetId);
                Conversation newConversation = Conversation.builder()
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
                Conversation savedConversation = conversationRepository.save(newConversation);
                if (chairman.getConversations() == null) {
                        chairman.setConversations(new HashSet<>());
                }
                chairman.getConversations().add(savedConversation);
                chairmanRepository.save(chairman);
                if ("USER".equals(targetType) || targetType == null) {
                        User target = userRepository.findById(new ObjectId(targetId))
                                        .orElseThrow(() -> new OperationException("создании диалога",
                                                        "Пользователь не найден: " + targetId, HttpStatus.NOT_FOUND));
                        if (target.getConversations() == null) {
                                target.setConversations(new HashSet<>());
                        }
                        target.getConversations().add(savedConversation);
                        userRepository.save(target);
                } else {
                        Chairman target = chairmanRepository.findById(targetId)
                                        .orElseThrow(() -> new OperationException("создании диалога",
                                                        "Председатель не найден: " + targetId, HttpStatus.NOT_FOUND));
                        if (target.getConversations() == null) {
                                target.setConversations(new HashSet<>());
                        }
                        target.getConversations().add(savedConversation);
                        chairmanRepository.save(target);
                }
                ChatMessage initMessage = ChatMessage.builder()
                                .conversation(savedConversation)
                                .content("")
                                .senderId(targetId)
                                .senderType(targetType != null ? targetType : "USER")
                                .createdAt(Instant.now())
                                .build();
                chatMessageRepository.save(initMessage);
                return savedConversation;
        }
        public List<ChatMessageResponse> getConversationMessages(String conversationId, int limit) {
                log.info("📥 Loading messages for conversation: {}, limit: {}", conversationId, limit);
                Chairman chairman = getCurrentUser();
                if (chairman.getConversations() == null ||
                                !chairman.getConversations().stream().anyMatch(c -> c.getId().equals(conversationId))) {
                        throw new OperationException("загрузке сообщений",
                                        "Нет доступа к диалогу", HttpStatus.FORBIDDEN);
                }
                List<ChatMessage> messages = chatMessageRepository.findByConversationId(
                                conversationId, PageRequest.of(0, limit));
                log.info("📨 Found {} messages for conversation {}", messages.size(), conversationId);
                Collections.reverse(messages);
                List<ChatMessage> filtered = messages.stream()
                                .filter(msg -> msg.getContent() != null && !msg.getContent().isEmpty())
                                .collect(Collectors.toList());
                return chatMessageMapper.toResponseList(filtered, chairman.getId());
        }
        public ChatConversationResponse getConversationInfo(String conversationId) {
                Chairman chairman = getCurrentUser();
                Conversation conversation = conversationRepository.findById(conversationId)
                                .orElseThrow(() -> new OperationException("получении диалога",
                                                "Диалог не найден: " + conversationId, HttpStatus.NOT_FOUND));
                if (chairman.getConversations() == null ||
                                !chairman.getConversations().stream().anyMatch(c -> c.getId().equals(conversationId))) {
                        throw new OperationException("получении диалога",
                                        "Нет доступа к диалогу", HttpStatus.FORBIDDEN);
                }
                return toConversationResponse(conversation, chairman.getId());
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
                if (otherId != null && "USER".equals(otherType)) {
                        userRepository.findById(new ObjectId(otherId)).ifPresent(u -> {
                        });
                        User u = userRepository.findById(new ObjectId(otherId)).orElse(null);
                        if (u != null) {
                                name = u.getFullName();
                                avatar = u.getPhoto();
                                online = u.isOnline();
                        }
                } else if (otherId != null && "CHAIRMAN".equals(otherType)) {
                        Chairman ch = chairmanRepository.findById(otherId).orElse(null);
                        if (ch != null) {
                                name = ch.getFullName();
                                avatar = ch.getPhoto();
                                online = ch.isOnline();
                        }
                }
                return ChatConversationResponse.builder()
                                .id(conversation.getId())
                                .participantId(otherId)
                                .name(name)
                                .avatar(avatar)
                                .lastMessage(lastMessage != null ? lastMessage.getContent() : "")
                                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt().toString() : "")
                                .online(online)
                                .participantType(otherType)
                                .build();
        }
        private ChatEvent buildChatEventForUser(ChatMessage message, User recipient, Chairman sender) {
                return ChatEvent.builder()
                                .eventType("MESSAGE_SENT")
                                .targetUserId(recipient.getId())
                                .targetUserRole("USER")
                                .conversationId(message.getConversation().getId())
                                .messageId(message.getId())
                                .senderId(sender.getId())
                                .senderName(sender.getFullName())
                                .senderAvatar(sender.getPhoto())
                                .senderType("CHAIRMAN")
                                .lastMessage(message.getContent())
                                .timestamp(message.getCreatedAt())
                                .isOnline(sender.isOnline())
                                .build();
        }
        private ChatEvent buildChatEventForChairman(ChatMessage message, Chairman recipient, Chairman sender) {
                return ChatEvent.builder()
                                .eventType("MESSAGE_SENT")
                                .targetUserId(recipient.getId())
                                .targetUserRole("CHAIRMAN")
                                .conversationId(message.getConversation().getId())
                                .messageId(message.getId())
                                .senderId(sender.getId())
                                .senderName(sender.getFullName())
                                .senderAvatar(sender.getPhoto())
                                .senderType("CHAIRMAN")
                                .lastMessage(message.getContent())
                                .timestamp(message.getCreatedAt())
                                .isOnline(sender.isOnline())
                                .build();
        }
}
