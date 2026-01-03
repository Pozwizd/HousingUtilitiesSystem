package org.spacelab.housingutilitiessystemuser.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.models.chat.event.ChatEvent;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatEventSubscriber Tests")
class ChatEventSubscriberTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatEventSubscriber chatEventSubscriber;

    private User testUser;
    private ChatEvent testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setLogin("testuser");
        testUser.setEmail("test@test.com");
        testUser.setOnline(true);

        testEvent = ChatEvent.builder()
                .eventType("MESSAGE_SENT")
                .conversationId("conv-123")
                .targetUserId("user-123")
                .targetUserRole("USER")
                .messageId("msg-123")
                .senderId("sender-123")
                .senderName("Test Sender")
                .senderType("USER")
                .lastMessage("Hello!")
                .isOnline(true)
                .build();
    }

    @Nested
    @DisplayName("Handle Message")
    class HandleMessage {
        @Test
        @DisplayName("Should handle message and send to online user")
        void handleMessage_shouldSendToOnlineUser() throws Exception {
            String json = "{\"targetUserId\":\"user-123\",\"targetUserRole\":\"USER\"}";
            when(objectMapper.readValue(json, ChatEvent.class)).thenReturn(testEvent);
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            chatEventSubscriber.handleMessage(json);

            verify(messagingTemplate).convertAndSendToUser(eq("testuser"), eq("/queue/sidebar"), eq(testEvent));
        }

        @Test
        @DisplayName("Should skip message for Chairman target role")
        void handleMessage_shouldSkipChairmanTarget() throws Exception {
            testEvent = ChatEvent.builder()
                    .targetUserId("chairman-123")
                    .targetUserRole("CHAIRMAN")
                    .build();
            String json = "{\"targetUserRole\":\"CHAIRMAN\"}";
            when(objectMapper.readValue(json, ChatEvent.class)).thenReturn(testEvent);

            chatEventSubscriber.handleMessage(json);

            verify(userRepository, never()).findById(any());
            verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
        }

        @Test
        @DisplayName("Should skip message when user not found")
        void handleMessage_shouldSkipWhenUserNotFound() throws Exception {
            String json = "{\"targetUserId\":\"unknown\",\"targetUserRole\":\"USER\"}";
            when(objectMapper.readValue(json, ChatEvent.class)).thenReturn(testEvent);
            when(userRepository.findById("user-123")).thenReturn(Optional.empty());

            chatEventSubscriber.handleMessage(json);

            verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
        }

        @Test
        @DisplayName("Should skip message when user is offline")
        void handleMessage_shouldSkipWhenUserOffline() throws Exception {
            testUser.setOnline(false);
            String json = "{\"targetUserId\":\"user-123\",\"targetUserRole\":\"USER\"}";
            when(objectMapper.readValue(json, ChatEvent.class)).thenReturn(testEvent);
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            chatEventSubscriber.handleMessage(json);

            verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
        }

        @Test
        @DisplayName("Should use email when login is null")
        void handleMessage_shouldUseEmailWhenLoginNull() throws Exception {
            testUser.setLogin(null);
            String json = "{\"targetUserId\":\"user-123\",\"targetUserRole\":\"USER\"}";
            when(objectMapper.readValue(json, ChatEvent.class)).thenReturn(testEvent);
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            chatEventSubscriber.handleMessage(json);

            verify(messagingTemplate).convertAndSendToUser(eq("test@test.com"), eq("/queue/sidebar"), eq(testEvent));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void handleMessage_shouldHandleException() throws Exception {
            String json = "invalid json";
            when(objectMapper.readValue(json, ChatEvent.class)).thenThrow(new RuntimeException("Parse error"));

            // Should not throw, just log error
            chatEventSubscriber.handleMessage(json);

            verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Handle Presence")
    class HandlePresence {
        @Test
        @DisplayName("Should broadcast presence event")
        void handlePresence_shouldBroadcast() throws Exception {
            ChatEvent presenceEvent = ChatEvent.builder()
                    .eventType("PRESENCE")
                    .targetUserId("user-123")
                    .isOnline(true)
                    .build();
            String json = "{\"targetUserId\":\"user-123\",\"isOnline\":true}";
            when(objectMapper.readValue(json, ChatEvent.class)).thenReturn(presenceEvent);

            chatEventSubscriber.handlePresence(json);

            verify(messagingTemplate).convertAndSend(eq("/topic/presence"), eq(presenceEvent));
        }

        @Test
        @DisplayName("Should broadcast offline status")
        void handlePresence_shouldBroadcastOffline() throws Exception {
            ChatEvent presenceEvent = ChatEvent.builder()
                    .eventType("PRESENCE")
                    .targetUserId("user-123")
                    .isOnline(false)
                    .build();
            String json = "{\"targetUserId\":\"user-123\",\"isOnline\":false}";
            when(objectMapper.readValue(json, ChatEvent.class)).thenReturn(presenceEvent);

            chatEventSubscriber.handlePresence(json);

            verify(messagingTemplate).convertAndSend(eq("/topic/presence"), eq(presenceEvent));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void handlePresence_shouldHandleException() throws Exception {
            String json = "invalid json";
            when(objectMapper.readValue(json, ChatEvent.class)).thenThrow(new RuntimeException("Parse error"));

            // Should not throw, just log error
            chatEventSubscriber.handlePresence(json);

            verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
        }
    }
}
