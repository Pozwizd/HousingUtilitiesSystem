package org.spacelab.housingutilitiessystemuser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemuser.entity.Chairman;
import org.spacelab.housingutilitiessystemuser.entity.location.House;
import org.spacelab.housingutilitiessystemuser.entity.location.Status;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.entity.chat.ChatMessage;
import org.spacelab.housingutilitiessystemuser.entity.chat.Conversation;
import org.spacelab.housingutilitiessystemuser.exception.OperationException;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatConversationResponse;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatMessageResponse;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatSidebarResponse;
import org.spacelab.housingutilitiessystemuser.repository.ChairmanRepository;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.spacelab.housingutilitiessystemuser.repository.chat.ChatMessageRepository;
import org.spacelab.housingutilitiessystemuser.repository.chat.ConversationRepository;
import org.spacelab.housingutilitiessystemuser.service.chat.ChatEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Tests")
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChairmanRepository chairmanRepository;

    @Mock
    private ChatEventPublisher chatEventPublisher;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ChatService chatService;

    private User testUser;
    private User otherUser;
    private Chairman testChairman;
    private Conversation testConversation;
    private Conversation testConversation2;
    private ChatMessage testMessage;
    private ChatMessage chairmanMessage;
    private House testHouse;

    @BeforeEach
    void setUp() {
        testHouse = new House();
        testHouse.setId("house-123");

        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("test@test.com");
        testUser.setLogin("testuser");
        testUser.setFirstName("Test");
        testUser.setMiddleName("M");
        testUser.setLastName("User");
        testUser.setHouse(testHouse);
        testUser.setOnline(true);
        testUser.setStatus(Status.ACTIVE);
        testUser.setConversations(new HashSet<>());

        otherUser = new User();
        otherUser.setId("other-user");
        otherUser.setEmail("other@test.com");
        otherUser.setFirstName("Other");
        otherUser.setMiddleName("O");
        otherUser.setLastName("User");
        otherUser.setOnline(true);
        otherUser.setStatus(Status.ACTIVE);
        otherUser.setConversations(new HashSet<>());

        testChairman = new Chairman();
        testChairman.setId("chairman-123");
        testChairman.setLogin("chairman");
        testChairman.setFirstName("Chairman");
        testChairman.setMiddleName("C");
        testChairman.setLastName("Test");
        testChairman.setOnline(true);
        testChairman.setConversations(new HashSet<>());

        testConversation = new Conversation();
        testConversation.setId("conv-123");
        testConversation.setCreatedAt(Instant.now());
        testConversation.setUpdatedAt(Instant.now());

        testConversation2 = new Conversation();
        testConversation2.setId("conv-456");
        testConversation2.setCreatedAt(Instant.now().minusSeconds(100));
        testConversation2.setUpdatedAt(Instant.now().minusSeconds(100));

        testMessage = ChatMessage.builder()
                .id("msg-123")
                .conversation(testConversation)
                .content("Hello!")
                .senderId("user-123")
                .senderType("USER")
                .createdAt(Instant.now())
                .build();

        chairmanMessage = ChatMessage.builder()
                .id("msg-chair")
                .conversation(testConversation)
                .senderId("chairman-123")
                .senderType("CHAIRMAN")
                .content("Hi from Chairman")
                .createdAt(Instant.now())
                .build();
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@test.com");
    }

    @Nested
    @DisplayName("Get Current User")
    class GetCurrentUser {
        @Test
        @DisplayName("Should get current authenticated user")
        void getCurrentUser_shouldReturnUser() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

                User result = chatService.getCurrentUser();

                assertThat(result).isNotNull();
                assertThat(result.getEmail()).isEqualTo("test@test.com");
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
        @DisplayName("Should throw when user not found")
        void getCurrentUser_shouldThrowWhenUserNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.getCurrentUser())
                        .isInstanceOf(OperationException.class);
            }
        }
    }

    @Nested
    @DisplayName("Get Chat Sidebar")
    class GetChatSidebar {
        @Test
        @DisplayName("Should return sidebar with conversations")
        void getChatSidebar_shouldReturnSidebar() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.of(testChairman));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
                assertThat(result.getChairman()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should handle user with null conversations")
        void getChatSidebar_shouldHandleNullConversations() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(null);
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.of(testChairman));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
                assertThat(result.getChatConversationResponses()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should handle user without house")
        void getChatSidebar_shouldHandleNoHouse() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setHouse(null);
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
                assertThat(result.getChairman()).isNull();
                assertThat(result.getChatContactResponses()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should exclude chairman already in conversation")
        void getChatSidebar_shouldExcludeChairmanInConversation() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.of(testChairman));
                when(chatMessageRepository.findLatestByConversationId("conv-123"))
                        .thenReturn(List.of(chairmanMessage));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
                assertThat(result.getChairman()).isNull(); // Chairman is already in conversation
            }
        }

        @Test
        @DisplayName("Should sort conversations by last message time")
        void getChatSidebar_shouldSortByLastMessageTime() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation, testConversation2)));
                ChatMessage olderMessage = ChatMessage.builder()
                        .id("msg-old")
                        .conversation(testConversation2)
                        .content("Older")
                        .senderId("chairman-123")
                        .senderType("CHAIRMAN")
                        .createdAt(Instant.now().minusSeconds(1000))
                        .build();
                        
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.empty());
                when(chatMessageRepository.findLatestByConversationId("conv-123"))
                        .thenReturn(List.of(testMessage));
                when(chatMessageRepository.findLatestByConversationId("conv-456"))
                        .thenReturn(List.of(olderMessage));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result.getChatConversationResponses()).hasSize(2);
            }
        }

        @Test
        @DisplayName("Should handle conversations with null last message time")
        void getChatSidebar_shouldHandleNullLastMessageTime() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation, testConversation2)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.empty());
                when(chatMessageRepository.findLatestByConversationId(anyString()))
                        .thenReturn(Collections.emptyList());

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result.getChatConversationResponses()).hasSize(2);
            }
        }

        @Test
        @DisplayName("Should filter inactive users from contacts")
        void getChatSidebar_shouldFilterInactiveUsers() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                User inactiveUser = new User();
                inactiveUser.setId("inactive-user");
                inactiveUser.setStatus(Status.BLOCKED);
                inactiveUser.setFirstName("Inactive");
                inactiveUser.setLastName("User");
                
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser, inactiveUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.of(testChairman));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result.getChatContactResponses()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should sort conversation with empty time to the end")
        void getChatSidebar_shouldSortEmptyTimeToEnd() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                Conversation conv1 = new Conversation();
                conv1.setId("conv-with-time");
                conv1.setCreatedAt(Instant.now());
                Conversation conv2 = new Conversation();
                conv2.setId("conv-no-time");
                conv2.setCreatedAt(Instant.now());
                
                testUser.setConversations(new HashSet<>(Set.of(conv1, conv2)));
                
                ChatMessage msgWithTime = ChatMessage.builder()
                        .id("msg-1")
                        .conversation(conv1)
                        .content("Hello")
                        .senderId("other-user")
                        .senderType("USER")
                        .createdAt(Instant.now())
                        .build();
                
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.empty());
                when(chatMessageRepository.findLatestByConversationId("conv-with-time"))
                        .thenReturn(List.of(msgWithTime));
                when(chatMessageRepository.findLatestByConversationId("conv-no-time"))
                        .thenReturn(Collections.emptyList());
                when(userRepository.findById("other-user")).thenReturn(Optional.of(otherUser));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result.getChatConversationResponses()).hasSize(2);
                // The one with time should be first
                assertThat(result.getChatConversationResponses().get(0).getId()).isEqualTo("conv-with-time");
            }
        }

        @Test
        @DisplayName("Should sort conversation with valid time before empty time")
        void getChatSidebar_shouldSortValidTimeFirst() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                Conversation conv1 = new Conversation();
                conv1.setId("conv-no-time-first");
                conv1.setCreatedAt(Instant.now());
                Conversation conv2 = new Conversation();
                conv2.setId("conv-with-time-second");
                conv2.setCreatedAt(Instant.now());
                
                testUser.setConversations(new HashSet<>(Set.of(conv1, conv2)));
                
                ChatMessage msgWithTime = ChatMessage.builder()
                        .id("msg-2")
                        .conversation(conv2)
                        .content("Hello")
                        .senderId("other-user")
                        .senderType("USER")
                        .createdAt(Instant.now())
                        .build();
                
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.empty());
                when(chatMessageRepository.findLatestByConversationId("conv-no-time-first"))
                        .thenReturn(Collections.emptyList());
                when(chatMessageRepository.findLatestByConversationId("conv-with-time-second"))
                        .thenReturn(List.of(msgWithTime));
                when(userRepository.findById("other-user")).thenReturn(Optional.of(otherUser));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result.getChatConversationResponses()).hasSize(2);
                // The one with time should be first
                assertThat(result.getChatConversationResponses().get(0).getId()).isEqualTo("conv-with-time-second");
            }
        }

        @Test
        @DisplayName("Should exclude users already in conversation from contacts")
        void getChatSidebar_shouldExcludeUsersInConversation() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                ChatMessage userMessage = ChatMessage.builder()
                        .id("msg-user")
                        .conversation(testConversation)
                        .senderId("other-user")
                        .senderType("USER")
                        .content("Hi")
                        .createdAt(Instant.now())
                        .build();
                        
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                otherUser.setHouse(testHouse);
                
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser, otherUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.of(testChairman));
                when(chatMessageRepository.findLatestByConversationId("conv-123"))
                        .thenReturn(List.of(userMessage));
                when(userRepository.findById("other-user")).thenReturn(Optional.of(otherUser));

                ChatSidebarResponse result = chatService.getChatSidebar();

                // Other user should not appear in contacts since they're already in conversation
                assertThat(result.getChatContactResponses()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should include active users not in conversation in contacts")
        void getChatSidebar_shouldIncludeActiveUsersNotInConversation() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                User activeUser = new User();
                activeUser.setId("active-not-in-conv");
                activeUser.setFirstName("Active");
                activeUser.setMiddleName("A");
                activeUser.setLastName("User");
                activeUser.setOnline(true);
                activeUser.setPhoto("photo.jpg");
                activeUser.setStatus(Status.ACTIVE);
                activeUser.setHouse(testHouse);
                
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser, activeUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.of(testChairman));

                ChatSidebarResponse result = chatService.getChatSidebar();

                // Active users not in conversation should be included in contacts
                assertThat(result.getChatContactResponses()).hasSize(1);
                assertThat(result.getChatContactResponses().get(0).getId()).isEqualTo("active-not-in-conv");
                assertThat(result.getChatContactResponses().get(0).getName()).isEqualTo("User Active A");
                assertThat(result.getChatContactResponses().get(0).getParticipantType()).isEqualTo("USER");
            }
        }

        @Test
        @DisplayName("Should handle otherId null in participants loop")
        void getChatSidebar_shouldHandleNullOtherIdInLoop() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                
                // Only current user's messages - no other participant
                ChatMessage ownMessage = ChatMessage.builder()
                        .id("msg-own")
                        .conversation(testConversation)
                        .senderId("user-123")
                        .senderType("USER")
                        .content("Hi")
                        .createdAt(Instant.now())
                        .build();
                
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(userRepository.findByHouse(testHouse)).thenReturn(List.of(testUser));
                when(chairmanRepository.findByHouseId("house-123")).thenReturn(Optional.of(testChairman));
                when(chatMessageRepository.findLatestByConversationId("conv-123"))
                        .thenReturn(List.of(ownMessage));

                ChatSidebarResponse result = chatService.getChatSidebar();

                assertThat(result).isNotNull();
                // Chairman should still appear since null otherId is not added to participants
                assertThat(result.getChairman()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Send Message")
    class SendMessage {
        @Test
        @DisplayName("Should send message successfully")
        void sendMessage_shouldSend() {
            testUser.setConversations(new HashSet<>(Set.of(testConversation)));
            when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(testMessage));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            ChatMessageResponse result = chatService.sendMessage("conv-123", "Hello!", testUser);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEqualTo("Hello!");
            verify(chatMessageRepository).save(any(ChatMessage.class));
        }

        @Test
        @DisplayName("Should throw when conversation not found")
        void sendMessage_shouldThrowWhenConversationNotFound() {
            when(conversationRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.sendMessage("unknown", "Hello!", testUser))
                    .isInstanceOf(OperationException.class);
        }

        @Test
        @DisplayName("Should throw when no access to conversation")
        void sendMessage_shouldThrowWhenNoAccess() {
            testUser.setConversations(new HashSet<>()); // Empty - no access
            when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));

            assertThatThrownBy(() -> chatService.sendMessage("conv-123", "Hello!", testUser))
                    .isInstanceOf(OperationException.class);
        }

        @Test
        @DisplayName("Should throw when sender conversations is null")
        void sendMessage_shouldThrowWhenNullConversations() {
            testUser.setConversations(null);
            when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));

            assertThatThrownBy(() -> chatService.sendMessage("conv-123", "Hello!", testUser))
                    .isInstanceOf(OperationException.class);
        }

        @Test
        @DisplayName("Should publish event to online Chairman recipient")
        void sendMessage_shouldPublishEventToChairman() {
            testUser.setConversations(new HashSet<>(Set.of(testConversation)));
            when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(testMessage, chairmanMessage));
            when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.of(testChairman));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            chatService.sendMessage("conv-123", "Hello!", testUser);

            verify(chatEventPublisher).publishMessageEvent(any());
        }

        @Test
        @DisplayName("Should publish event to online User recipient")
        void sendMessage_shouldPublishEventToUser() {
            testUser.setConversations(new HashSet<>(Set.of(testConversation)));
            ChatMessage userMessage = ChatMessage.builder()
                    .id("msg-user")
                    .conversation(testConversation)
                    .senderId("other-user")
                    .senderType("USER")
                    .content("")
                    .createdAt(Instant.now())
                    .build();

            when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(testMessage, userMessage));
            when(userRepository.findById("other-user")).thenReturn(Optional.of(otherUser));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            chatService.sendMessage("conv-123", "Hello!", testUser);

            verify(chatEventPublisher).publishMessageEvent(any());
        }

        @Test
        @DisplayName("Should not publish event when Chairman is offline")
        void sendMessage_shouldNotPublishWhenChairmanOffline() {
            testChairman.setOnline(false);
            testUser.setConversations(new HashSet<>(Set.of(testConversation)));
            when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(testMessage, chairmanMessage));
            when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.of(testChairman));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            chatService.sendMessage("conv-123", "Hello!", testUser);

            verify(chatEventPublisher, never()).publishMessageEvent(any());
        }

        @Test
        @DisplayName("Should not publish event when User is offline")
        void sendMessage_shouldNotPublishWhenUserOffline() {
            otherUser.setOnline(false);
            testUser.setConversations(new HashSet<>(Set.of(testConversation)));
            ChatMessage userMessage = ChatMessage.builder()
                    .id("msg-user")
                    .conversation(testConversation)
                    .senderId("other-user")
                    .senderType("USER")
                    .content("")
                    .createdAt(Instant.now())
                    .build();

            when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(testMessage, userMessage));
            when(userRepository.findById("other-user")).thenReturn(Optional.of(otherUser));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            chatService.sendMessage("conv-123", "Hello!", testUser);

            verify(chatEventPublisher, never()).publishMessageEvent(any());
        }

        @Test
        @DisplayName("Should handle no recipient found")
        void sendMessage_shouldHandleNoRecipient() {
            testUser.setConversations(new HashSet<>(Set.of(testConversation)));
            when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
            // Only sender's message - no other participant
            when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(testMessage));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            ChatMessageResponse result = chatService.sendMessage("conv-123", "Hello!", testUser);

            assertThat(result).isNotNull();
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
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(chatMessageRepository.findLatestByConversationId("conv-123"))
                        .thenReturn(List.of(chairmanMessage));

                Conversation result = chatService.getOrCreateConversation("chairman-123", "CHAIRMAN");

                assertThat(result).isEqualTo(testConversation);
                verify(conversationRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should create new conversation with Chairman")
        void getOrCreateConversation_shouldCreateWithChairman() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.of(testChairman));

                Conversation result = chatService.getOrCreateConversation("chairman-123", "CHAIRMAN");

                assertThat(result).isNotNull();
                verify(conversationRepository).save(any(Conversation.class));
                verify(userRepository).save(testUser);
                verify(chairmanRepository).save(testChairman);
            }
        }

        @Test
        @DisplayName("Should create new conversation with User")
        void getOrCreateConversation_shouldCreateWithUser() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(userRepository.findById("other-user")).thenReturn(Optional.of(otherUser));

                Conversation result = chatService.getOrCreateConversation("other-user", "USER");

                assertThat(result).isNotNull();
                verify(conversationRepository).save(any(Conversation.class));
            }
        }

        @Test
        @DisplayName("Should throw when Chairman not found")
        void getOrCreateConversation_shouldThrowWhenChairmanNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(chairmanRepository.findById("unknown")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.getOrCreateConversation("unknown", "CHAIRMAN"))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should throw when target User not found")
        void getOrCreateConversation_shouldThrowWhenUserNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(userRepository.findById("unknown")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.getOrCreateConversation("unknown", "USER"))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should initialize null conversations for current user")
        void getOrCreateConversation_shouldInitNullConversationsForCurrentUser() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(null);
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.of(testChairman));

                Conversation result = chatService.getOrCreateConversation("chairman-123", "CHAIRMAN");

                assertThat(result).isNotNull();
                assertThat(testUser.getConversations()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should initialize null conversations for Chairman target")
        void getOrCreateConversation_shouldInitNullConversationsForChairman() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testChairman.setConversations(null);
                testUser.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.of(testChairman));

                Conversation result = chatService.getOrCreateConversation("chairman-123", "CHAIRMAN");

                assertThat(result).isNotNull();
                assertThat(testChairman.getConversations()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should initialize null conversations for User target")
        void getOrCreateConversation_shouldInitNullConversationsForUser() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                otherUser.setConversations(null);
                testUser.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
                when(userRepository.findById("other-user")).thenReturn(Optional.of(otherUser));

                Conversation result = chatService.getOrCreateConversation("other-user", "USER");

                assertThat(result).isNotNull();
                assertThat(otherUser.getConversations()).isNotNull();
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
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(chatMessageRepository.findByConversationId(eq("conv-123"), any(Pageable.class)))
                        .thenReturn(new ArrayList<>(List.of(testMessage)));
                when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

                List<ChatMessageResponse> result = chatService.getConversationMessages("conv-123", 50);

                assertThat(result).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should throw when no access")
        void getConversationMessages_shouldThrowWhenNoAccess() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

                assertThatThrownBy(() -> chatService.getConversationMessages("conv-123", 50))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should throw when conversations is null")
        void getConversationMessages_shouldThrowWhenNullConversations() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(null);
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

                assertThatThrownBy(() -> chatService.getConversationMessages("conv-123", 50))
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
                        .senderId("user-123")
                        .senderType("USER")
                        .createdAt(Instant.now())
                        .build();

                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(chatMessageRepository.findByConversationId(eq("conv-123"), any(Pageable.class)))
                        .thenReturn(new ArrayList<>(List.of(testMessage, emptyMessage)));
                when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

                List<ChatMessageResponse> result = chatService.getConversationMessages("conv-123", 50);

                assertThat(result).hasSize(1); // Only non-empty message
            }
        }

        @Test
        @DisplayName("Should filter null content messages")
        void getConversationMessages_shouldFilterNullContent() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                ChatMessage nullContentMessage = ChatMessage.builder()
                        .id("msg-null")
                        .conversation(testConversation)
                        .content(null)
                        .senderId("user-123")
                        .senderType("USER")
                        .createdAt(Instant.now())
                        .build();

                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(chatMessageRepository.findByConversationId(eq("conv-123"), any(Pageable.class)))
                        .thenReturn(new ArrayList<>(List.of(testMessage, nullContentMessage)));
                when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

                List<ChatMessageResponse> result = chatService.getConversationMessages("conv-123", 50);

                assertThat(result).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should handle Chairman sender in messages")
        void getConversationMessages_shouldHandleChairmanSender() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(chatMessageRepository.findByConversationId(eq("conv-123"), any(Pageable.class)))
                        .thenReturn(new ArrayList<>(List.of(chairmanMessage)));
                when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.of(testChairman));

                List<ChatMessageResponse> result = chatService.getConversationMessages("conv-123", 50);

                assertThat(result).hasSize(1);
                assertThat(result.get(0).getSenderType()).isEqualTo("CHAIRMAN");
            }
        }

        @Test
        @DisplayName("Should handle Chairman not found in messages")
        void getConversationMessages_shouldHandleChairmanNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(chatMessageRepository.findByConversationId(eq("conv-123"), any(Pageable.class)))
                        .thenReturn(new ArrayList<>(List.of(chairmanMessage)));
                when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.empty());

                List<ChatMessageResponse> result = chatService.getConversationMessages("conv-123", 50);

                assertThat(result).hasSize(1);
                assertThat(result.get(0).getSenderName()).isEqualTo("Председатель");
            }
        }

        @Test
        @DisplayName("Should handle User not found in messages")
        void getConversationMessages_shouldHandleUserNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(chatMessageRepository.findByConversationId(eq("conv-123"), any(Pageable.class)))
                        .thenReturn(new ArrayList<>(List.of(testMessage)));
                when(userRepository.findById("user-123")).thenReturn(Optional.empty());

                List<ChatMessageResponse> result = chatService.getConversationMessages("conv-123", 50);

                assertThat(result).hasSize(1);
                assertThat(result.get(0).getSenderName()).isEqualTo("Пользователь");
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
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(testMessage));

                ChatConversationResponse result = chatService.getConversationInfo("conv-123");

                assertThat(result).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw when conversation not found")
        void getConversationInfo_shouldThrowWhenNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.findById("unknown")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.getConversationInfo("unknown"))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should throw when no access")
        void getConversationInfo_shouldThrowWhenNoAccess() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>());
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));

                assertThatThrownBy(() -> chatService.getConversationInfo("conv-123"))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should throw when conversations is null")
        void getConversationInfo_shouldThrowWhenNullConversations() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(null);
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));

                assertThatThrownBy(() -> chatService.getConversationInfo("conv-123"))
                        .isInstanceOf(OperationException.class);
            }
        }

        @Test
        @DisplayName("Should return info with Chairman participant")
        void getConversationInfo_shouldReturnWithChairman() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(chairmanMessage));
                when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.of(testChairman));

                ChatConversationResponse result = chatService.getConversationInfo("conv-123");

                assertThat(result).isNotNull();
                assertThat(result.getParticipantType()).isEqualTo("CHAIRMAN");
            }
        }

        @Test
        @DisplayName("Should return info with User participant")
        void getConversationInfo_shouldReturnWithUser() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                ChatMessage otherUserMessage = ChatMessage.builder()
                        .id("msg-other")
                        .conversation(testConversation)
                        .senderId("other-user")
                        .senderType("USER")
                        .content("Hello")
                        .createdAt(Instant.now())
                        .build();
                        
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(otherUserMessage));
                when(userRepository.findById("other-user")).thenReturn(Optional.of(otherUser));

                ChatConversationResponse result = chatService.getConversationInfo("conv-123");

                assertThat(result).isNotNull();
                assertThat(result.getParticipantType()).isEqualTo("USER");
            }
        }

        @Test
        @DisplayName("Should handle empty messages list")
        void getConversationInfo_shouldHandleEmptyMessages() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(Collections.emptyList());

                ChatConversationResponse result = chatService.getConversationInfo("conv-123");

                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("Новый диалог");
                assertThat(result.getLastMessage()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should handle Chairman not found in conversation info")
        void getConversationInfo_shouldHandleChairmanNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(chairmanMessage));
                when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.empty());

                ChatConversationResponse result = chatService.getConversationInfo("conv-123");

                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("Новый диалог");
            }
        }

        @Test
        @DisplayName("Should handle User not found in conversation info")
        void getConversationInfo_shouldHandleUserNotFound() {
            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                ChatMessage otherUserMessage = ChatMessage.builder()
                        .id("msg-other")
                        .conversation(testConversation)
                        .senderId("other-user")
                        .senderType("USER")
                        .content("Hello")
                        .createdAt(Instant.now())
                        .build();
                        
                testUser.setConversations(new HashSet<>(Set.of(testConversation)));
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                mockSecurityContext();
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
                when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
                when(chatMessageRepository.findLatestByConversationId("conv-123")).thenReturn(List.of(otherUserMessage));
                when(userRepository.findById("other-user")).thenReturn(Optional.empty());

                ChatConversationResponse result = chatService.getConversationInfo("conv-123");

                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("Новый диалог");
            }
        }
    }
}
