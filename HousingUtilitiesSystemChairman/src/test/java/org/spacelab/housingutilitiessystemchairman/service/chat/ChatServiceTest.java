package org.spacelab.housingutilitiessystemchairman.service.chat;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.entity.chat.ChatMessage;
import org.spacelab.housingutilitiessystemchairman.entity.chat.Conversation;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.entity.location.Status;
import org.spacelab.housingutilitiessystemchairman.exception.OperationException;
import org.spacelab.housingutilitiessystemchairman.mappers.chat.ChatMessageMapper;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatConversationResponse;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatMessageResponse;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatSidebarResponse;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.HouseRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.UserRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.chat.ChatMessageRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.chat.ConversationRepository;
import org.spacelab.housingutilitiessystemchairman.service.ChairmanService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Tests")
class ChatServiceTest {

    @Mock
    private ChairmanService chairmanService;

    @Mock
    private ChairmanRepository chairmanRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private HouseRepository houseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private ChatEventPublisher chatEventPublisher;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ChatService chatService;

    private Chairman testChairman;
    private Chairman otherChairman;
    private User testUser;
    private User otherUser;
    private Conversation testConversation;
    private Conversation testConversation2;
    private ChatMessage testMessage;
    private ChatMessage userMessage;
    private ChatMessage chairmanMessage;
    private House testHouse;

    @BeforeEach
    void setUp() {
        // Use valid 24-character hex strings for MongoDB ObjectId
        String houseId = "507f1f77bcf86cd799439011";
        String chairmanId = "507f1f77bcf86cd799439012";
        String otherChairmanId = "507f1f77bcf86cd799439013";
        String userId = "507f1f77bcf86cd799439014";
        String otherUserId = "507f1f77bcf86cd799439015";
        String convId = "507f1f77bcf86cd799439016";
        String convId2 = "507f1f77bcf86cd799439017";
        String msgId = "507f1f77bcf86cd799439018";
        String msgUserId = "507f1f77bcf86cd799439019";
        String msgChairmanId = "507f1f77bcf86cd79943901a";

        testHouse = new House();
        testHouse.setId(houseId);

        testChairman = new Chairman();
        testChairman.setId(chairmanId);
        testChairman.setLogin("chairman");
        testChairman.setEmail("chairman@test.com");
        testChairman.setFirstName("Chairman");
        testChairman.setMiddleName("C");
        testChairman.setLastName("Test");
        testChairman.setOnline(true);
        testChairman.setConversations(new HashSet<>());

        otherChairman = new Chairman();
        otherChairman.setId(otherChairmanId);
        otherChairman.setLogin("other");
        otherChairman.setFirstName("Other");
        otherChairman.setMiddleName("O");
        otherChairman.setLastName("Chairman");
        otherChairman.setOnline(true);
        otherChairman.setConversations(new HashSet<>());

        testUser = new User();
        testUser.setId(userId);
        testUser.setLogin("testuser");
        testUser.setFirstName("Test");
        testUser.setMiddleName("M");
        testUser.setLastName("User");
        testUser.setOnline(true);
        testUser.setStatus(Status.ACTIVE);
        testUser.setHouse(testHouse);
        testUser.setConversations(new HashSet<>());

        otherUser = new User();
        otherUser.setId(otherUserId);
        otherUser.setLogin("other");
        otherUser.setFirstName("Other");
        otherUser.setMiddleName("O");
        otherUser.setLastName("User");
        otherUser.setOnline(true);
        otherUser.setStatus(Status.ACTIVE);
        otherUser.setConversations(new HashSet<>());

        testConversation = new Conversation();
        testConversation.setId(convId);
        testConversation.setCreatedAt(Instant.now());
        testConversation.setUpdatedAt(Instant.now());

        testConversation2 = new Conversation();
        testConversation2.setId(convId2);
        testConversation2.setCreatedAt(Instant.now().minusSeconds(100));
        testConversation2.setUpdatedAt(Instant.now().minusSeconds(100));

        testMessage = ChatMessage.builder()
                .id(msgId)
                .conversation(testConversation)
                .content("Hello!")
                .senderId(chairmanId)
                .senderType("CHAIRMAN")
                .createdAt(Instant.now())
                .build();

        userMessage = ChatMessage.builder()
                .id(msgUserId)
                .conversation(testConversation)
                .content("Hi from user")
                .senderId(userId)
                .senderType("USER")
                .createdAt(Instant.now())
                .build();

        chairmanMessage = ChatMessage.builder()
                .id(msgChairmanId)
                .conversation(testConversation)
                .content("Hi from chairman")
                .senderId(otherChairmanId)
                .senderType("CHAIRMAN")
                .createdAt(Instant.now())
                .build();
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("chairman@test.com");
    }

    @Nested
    @DisplayName("Get Current User")
    class GetCurrentUser {
        @Test
        @DisplayName("Should get current authenticated chairman by email")
        void getCurrentUser_shouldReturnChairmanByEmail() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));

                Chairman result = chatService.getCurrentUser();

                assertThat(result).isNotNull();
                assertThat(result.getEmail()).isEqualTo("chairman@test.com");
            }
        }

        @Test
        @DisplayName("Should get current authenticated chairman by login")
        void getCurrentUser_shouldReturnChairmanByLogin() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.empty());
                when(chairmanService.findByLogin("chairman@test.com")).thenReturn(Optional.of(testChairman));

                Chairman result = chatService.getCurrentUser();

                assertThat(result).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw when not authenticated")
        void getCurrentUser_shouldThrowWhenNotAuthenticated() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                when(securityContext.getAuthentication()).thenReturn(null);

                assertThatThrownBy(() -> chatService.getCurrentUser())
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should throw when authentication is not authenticated")
        void getCurrentUser_shouldThrowWhenNotAuthenticated2() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.isAuthenticated()).thenReturn(false);

                assertThatThrownBy(() -> chatService.getCurrentUser())
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should throw when chairman not found")
        void getCurrentUser_shouldThrowWhenChairmanNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.empty());
                when(chairmanService.findByLogin("chairman@test.com")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.getCurrentUser())
                        .isInstanceOf(OperationException.class);
            }
        }
    }

    @Nested
    @DisplayName("Get Chat Sidebar")
    class GetChatSidebar {
        @Test
        @DisplayName("Should return sidebar with conversations and contacts")
        void getChatSidebar_shouldReturnSidebar() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(chatMessageRepository.findByConversationIdIn(anyList())).thenReturn(List.of(userMessage));
                when(userRepository.findAllById(anyList())).thenReturn(List.of(testUser));
                when(houseRepository.findByChairman(testChairman)).thenReturn(Set.of(testHouse));
                when(userRepository.findByHouseIn(anyList())).thenReturn(List.of(testUser, otherUser));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
                assertThat(result.getChatConversationResponses()).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should handle null conversations")
        void getChatSidebar_shouldHandleNullConversations() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(null);
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(houseRepository.findByChairman(testChairman)).thenReturn(Set.of(testHouse));
                when(userRepository.findByHouseIn(anyList())).thenReturn(List.of(testUser));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
                assertThat(result.getChatConversationResponses()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should handle empty houses")
        void getChatSidebar_shouldHandleEmptyHouses() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(houseRepository.findByChairman(testChairman)).thenReturn(Collections.emptySet());

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
                assertThat(result.getChatContactResponses()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should handle chairman as participant in conversation")
        void getChatSidebar_shouldHandleChairmanParticipant() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(chatMessageRepository.findByConversationIdIn(anyList())).thenReturn(List.of(chairmanMessage));
                when(chairmanRepository.findAllById(anyIterable())).thenReturn(List.of(otherChairman));
                when(houseRepository.findByChairman(testChairman)).thenReturn(Collections.emptySet());

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
            }
        }

        @Test
        @DisplayName("Should search participant in all messages when not in last message")
        void getChatSidebar_shouldSearchInAllMessages() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                // Return message from current chairman, so participant not found in last messages
                when(chatMessageRepository.findByConversationIdIn(anyList())).thenReturn(List.of(testMessage));
                // Return user message when searching in all messages
                when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(userMessage));
                when(userRepository.findAllById(anyList())).thenReturn(List.of(testUser));
                when(houseRepository.findByChairman(testChairman)).thenReturn(Collections.emptySet());

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
            }
        }

        @Test
        @DisplayName("Should filter inactive users from contacts")
        void getChatSidebar_shouldFilterInactiveUsers() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                User inactiveUser = new User();
                inactiveUser.setId("507f1f77bcf86cd79943901b");
                inactiveUser.setStatus(Status.BLOCKED);

                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(houseRepository.findByChairman(testChairman)).thenReturn(Set.of(testHouse));
                when(userRepository.findByHouseIn(anyList())).thenReturn(List.of(inactiveUser));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result.getChatContactResponses()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should sort conversations by last message time")
        void getChatSidebar_shouldSortConversations() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation, testConversation2)));
                ChatMessage olderMessage = ChatMessage.builder()
                        .id("msg-old")
                        .conversation(testConversation2)
                        .content("Old")
                        .senderId("507f1f77bcf86cd799439014")
                        .senderType("USER")
                        .createdAt(Instant.now().minusSeconds(1000))
                        .build();

                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(chatMessageRepository.findByConversationIdIn(anyList())).thenReturn(List.of(userMessage, olderMessage));
                when(userRepository.findAllById(anyList())).thenReturn(List.of(testUser));
                when(houseRepository.findByChairman(testChairman)).thenReturn(Collections.emptySet());

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result.getChatConversationResponses()).hasSize(2);
            }
        }
    }

    @Nested
    @DisplayName("Send Message")
    class SendMessage {
        @Test
        @DisplayName("Should send message successfully")
        void sendMessage_shouldSend() {
            testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
            ChatMessageResponse expectedResponse = ChatMessageResponse.builder()
                    .id("msg-123")
                    .content("Hello!")
                    .build();

            when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(userMessage));
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(chatMessageMapper.toResponse(any(), anyString())).thenReturn(expectedResponse);

            ChatMessageResponse result = chatService.sendMessage("507f1f77bcf86cd799439016", "Hello!", testChairman);

            assertThat(result).isNotNull();
            verify(chatMessageRepository).save(any(ChatMessage.class));
            verify(chatEventPublisher).publishMessageEvent(any());
        }

        @Test
        @DisplayName("Should throw when conversation not found")
        void sendMessage_shouldThrowWhenConversationNotFound() {
            when(conversationRepository.findById("507f1f77bcf86cd799439fff")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.sendMessage("507f1f77bcf86cd799439fff", "Hello!", testChairman))
                    .isInstanceOf(OperationException.class);
        }

        @Test
        @DisplayName("Should throw when no access to conversation")
        void sendMessage_shouldThrowWhenNoAccess() {
            testChairman.setConversations(new HashSet<>());
            when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));

            assertThatThrownBy(() -> chatService.sendMessage("507f1f77bcf86cd799439016", "Hello!", testChairman))
                    .isInstanceOf(OperationException.class);
        }

        @Test
        @DisplayName("Should throw when sender has null conversations")
        void sendMessage_shouldThrowWhenNullConversations() {
            testChairman.setConversations(null);
            when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));

            assertThatThrownBy(() -> chatService.sendMessage("507f1f77bcf86cd799439016", "Hello!", testChairman))
                    .isInstanceOf(OperationException.class);
        }

        @Test
        @DisplayName("Should publish event to online User recipient")
        void sendMessage_shouldPublishEventToUser() {
            testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
            ChatMessageResponse expectedResponse = ChatMessageResponse.builder().id("msg-123").build();

            when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(userMessage));
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(chatMessageMapper.toResponse(any(), anyString())).thenReturn(expectedResponse);

            chatService.sendMessage("507f1f77bcf86cd799439016", "Hello!", testChairman);

            verify(chatEventPublisher).publishMessageEvent(any());
        }

        @Test
        @DisplayName("Should publish event to online Chairman recipient")
        void sendMessage_shouldPublishEventToChairman() {
            testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
            ChatMessageResponse expectedResponse = ChatMessageResponse.builder().id("msg-123").build();

            when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(chairmanMessage));
            when(chairmanRepository.findById("507f1f77bcf86cd799439013")).thenReturn(Optional.of(otherChairman));
            when(chatMessageMapper.toResponse(any(), anyString())).thenReturn(expectedResponse);

            chatService.sendMessage("507f1f77bcf86cd799439016", "Hello!", testChairman);

            verify(chatEventPublisher).publishMessageEvent(any());
        }

        @Test
        @DisplayName("Should not publish event when User is offline")
        void sendMessage_shouldNotPublishWhenUserOffline() {
            testUser.setOnline(false);
            testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
            ChatMessageResponse expectedResponse = ChatMessageResponse.builder().id("msg-123").build();

            when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(userMessage));
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(chatMessageMapper.toResponse(any(), anyString())).thenReturn(expectedResponse);

            chatService.sendMessage("507f1f77bcf86cd799439016", "Hello!", testChairman);

            verify(chatEventPublisher, never()).publishMessageEvent(any());
        }

        @Test
        @DisplayName("Should not publish event when Chairman is offline")
        void sendMessage_shouldNotPublishWhenChairmanOffline() {
            otherChairman.setOnline(false);
            testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
            ChatMessageResponse expectedResponse = ChatMessageResponse.builder().id("msg-123").build();

            when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(chairmanMessage));
            when(chairmanRepository.findById("507f1f77bcf86cd799439013")).thenReturn(Optional.of(otherChairman));
            when(chatMessageMapper.toResponse(any(), anyString())).thenReturn(expectedResponse);

            chatService.sendMessage("507f1f77bcf86cd799439016", "Hello!", testChairman);

            verify(chatEventPublisher, never()).publishMessageEvent(any());
        }
    }

    @Nested
    @DisplayName("Get Or Create Conversation")
    class GetOrCreateConversation {
        @Test
        @DisplayName("Should return existing conversation")
        void getOrCreateConversation_shouldReturnExisting() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(userMessage));

                Conversation result = chatService.getOrCreateConversation("507f1f77bcf86cd799439014", "USER");

                assertThat(result).isEqualTo(testConversation);
                verify(conversationRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should create new conversation with User")
        void getOrCreateConversation_shouldCreateWithUser() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));

                Conversation result = chatService.getOrCreateConversation("507f1f77bcf86cd799439014", "USER");

                assertThat(result).isNotNull();
                verify(conversationRepository).save(any(Conversation.class));
            }
        }

        @Test
        @DisplayName("Should create new conversation with null targetType (defaults to USER)")
        void getOrCreateConversation_shouldCreateWithNullTargetType() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));

                Conversation result = chatService.getOrCreateConversation("507f1f77bcf86cd799439014", null);

                assertThat(result).isNotNull();
                verify(userRepository).findById(any(ObjectId.class));
            }
        }

        @Test
        @DisplayName("Should create new conversation with Chairman")
        void getOrCreateConversation_shouldCreateWithChairman() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(chairmanRepository.findById("507f1f77bcf86cd799439013")).thenReturn(Optional.of(otherChairman));

                Conversation result = chatService.getOrCreateConversation("507f1f77bcf86cd799439013", "CHAIRMAN");

                assertThat(result).isNotNull();
                verify(chairmanRepository, times(2)).save(any(Chairman.class));
            }
        }

        @Test
        @DisplayName("Should throw when User not found")
        void getOrCreateConversation_shouldThrowWhenUserNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.getOrCreateConversation("507f1f77bcf86cd799439fff", "USER"))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should throw when Chairman not found")
        void getOrCreateConversation_shouldThrowWhenChairmanNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(chairmanRepository.findById("507f1f77bcf86cd799439fff")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.getOrCreateConversation("507f1f77bcf86cd799439fff", "CHAIRMAN"))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should initialize null conversations for chairman")
        void getOrCreateConversation_shouldInitNullConversationsForChairman() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(null);
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));

                Conversation result = chatService.getOrCreateConversation("507f1f77bcf86cd799439014", "USER");

                assertThat(result).isNotNull();
                assertThat(testChairman.getConversations()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should initialize null conversations for target User")
        void getOrCreateConversation_shouldInitNullConversationsForUser() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(null);
                testChairman.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));

                Conversation result = chatService.getOrCreateConversation("507f1f77bcf86cd799439014", "USER");

                assertThat(result).isNotNull();
                assertThat(testUser.getConversations()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should initialize null conversations for target Chairman")
        void getOrCreateConversation_shouldInitNullConversationsForTargetChairman() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                otherChairman.setConversations(null);
                testChairman.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(chairmanRepository.findById("507f1f77bcf86cd799439013")).thenReturn(Optional.of(otherChairman));

                Conversation result = chatService.getOrCreateConversation("507f1f77bcf86cd799439013", "CHAIRMAN");

                assertThat(result).isNotNull();
                assertThat(otherChairman.getConversations()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Get Conversation Messages")
    class GetConversationMessages {
        @Test
        @DisplayName("Should return messages")
        void getConversationMessages_shouldReturn() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(chatMessageRepository.findByConversationId(eq("507f1f77bcf86cd799439016"), any(Pageable.class)))
                        .thenReturn(new ArrayList<>(List.of(testMessage)));
                when(chatMessageMapper.toResponseList(anyList(), anyString()))
                        .thenReturn(List.of(ChatMessageResponse.builder().id("msg-123").build()));

                List<ChatMessageResponse> result = chatService.getConversationMessages("507f1f77bcf86cd799439016", 50);

                assertThat(result).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should throw when no access")
        void getConversationMessages_shouldThrowWhenNoAccess() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));

                assertThatThrownBy(() -> chatService.getConversationMessages("507f1f77bcf86cd799439016", 50))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should throw when conversations is null")
        void getConversationMessages_shouldThrowWhenNullConversations() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(null);
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));

                assertThatThrownBy(() -> chatService.getConversationMessages("507f1f77bcf86cd799439016", 50))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should filter empty messages")
        void getConversationMessages_shouldFilterEmpty() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                ChatMessage emptyMessage = ChatMessage.builder()
                        .id("msg-empty")
                        .conversation(testConversation)
                        .content("")
                        .senderId("507f1f77bcf86cd799439012")
                        .senderType("CHAIRMAN")
                        .createdAt(Instant.now())
                        .build();

                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(chatMessageRepository.findByConversationId(eq("507f1f77bcf86cd799439016"), any(Pageable.class)))
                        .thenReturn(new ArrayList<>(List.of(testMessage, emptyMessage)));
                when(chatMessageMapper.toResponseList(anyList(), anyString()))
                        .thenReturn(List.of(ChatMessageResponse.builder().id("msg-123").build()));

                List<ChatMessageResponse> result = chatService.getConversationMessages("507f1f77bcf86cd799439016", 50);

                assertThat(result).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("Get Conversation Info")
    class GetConversationInfo {
        @Test
        @DisplayName("Should return conversation info")
        void getConversationInfo_shouldReturn() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(userMessage));
                when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));

                ChatConversationResponse result = chatService.getConversationInfo("507f1f77bcf86cd799439016");

                assertThat(result).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw when conversation not found")
        void getConversationInfo_shouldThrowWhenNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.findById("507f1f77bcf86cd799439fff")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.getConversationInfo("507f1f77bcf86cd799439fff"))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should throw when no access")
        void getConversationInfo_shouldThrowWhenNoAccess() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));

                assertThatThrownBy(() -> chatService.getConversationInfo("507f1f77bcf86cd799439016"))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should return info with User participant")
        void getConversationInfo_shouldReturnWithUser() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(userMessage));
                when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));

                ChatConversationResponse result = chatService.getConversationInfo("507f1f77bcf86cd799439016");

                assertThat(result.getParticipantType()).isEqualTo("USER");
            }
        }

        @Test
        @DisplayName("Should return info with Chairman participant")
        void getConversationInfo_shouldReturnWithChairman() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(chairmanMessage));
                when(chairmanRepository.findById("507f1f77bcf86cd799439013")).thenReturn(Optional.of(otherChairman));

                ChatConversationResponse result = chatService.getConversationInfo("507f1f77bcf86cd799439016");

                assertThat(result.getParticipantType()).isEqualTo("CHAIRMAN");
            }
        }

        @Test
        @DisplayName("Should handle empty messages")
        void getConversationInfo_shouldHandleEmptyMessages() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(Collections.emptyList());

                ChatConversationResponse result = chatService.getConversationInfo("507f1f77bcf86cd799439016");

                assertThat(result.getName()).isEqualTo("Новый диалог");
                assertThat(result.getLastMessage()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should handle User not found")
        void getConversationInfo_shouldHandleUserNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(userMessage));
                when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

                ChatConversationResponse result = chatService.getConversationInfo("507f1f77bcf86cd799439016");

                assertThat(result.getName()).isEqualTo("Новый диалог");
            }
        }

        @Test
        @DisplayName("Should handle Chairman not found")
        void getConversationInfo_shouldHandleChairmanNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(chairmanService.findByEmail("chairman@test.com")).thenReturn(Optional.of(testChairman));
                when(conversationRepository.findById("507f1f77bcf86cd799439016")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("507f1f77bcf86cd799439016")).thenReturn(List.of(chairmanMessage));
                when(chairmanRepository.findById("507f1f77bcf86cd799439013")).thenReturn(Optional.empty());

                ChatConversationResponse result = chatService.getConversationInfo("507f1f77bcf86cd799439016");

                assertThat(result.getName()).isEqualTo("Новый диалог");
            }
        }
    }
}
