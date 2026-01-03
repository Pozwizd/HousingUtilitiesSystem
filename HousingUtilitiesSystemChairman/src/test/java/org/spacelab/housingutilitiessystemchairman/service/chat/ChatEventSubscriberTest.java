package org.spacelab.housingutilitiessystemchairman.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.models.chat.event.ChatEvent;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatEventSubscriber Tests")
class ChatEventSubscriberTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChairmanRepository chairmanRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatEventSubscriber chatEventSubscriber;

    private ChatEvent testEvent;
    private Chairman testChairman;
    private String testMessageJson;

    @BeforeEach
    void setUp() {
        testEvent = ChatEvent.builder()
                .eventType("MESSAGE_SENT")
                .targetUserId("chairman-123")
                .targetUserRole("CHAIRMAN")
                .conversationId("conv-123")
                .messageId("msg-123")
                .senderId("user-123")
                .senderName("Test User")
                .senderType("USER")
                .lastMessage("Hello!")
                .timestamp(Instant.now())
                .isOnline(true)
                .build();

        testChairman = new Chairman();
        testChairman.setId("chairman-123");
        testChairman.setLogin("chairman");
        testChairman.setOnline(true);

        testMessageJson = "{\"eventType\":\"MESSAGE_SENT\",\"targetUserId\":\"chairman-123\",\"targetUserRole\":\"CHAIRMAN\"}";
    }

    @Nested
    @DisplayName("Handle Message")
    class HandleMessage {
        @Test
        @DisplayName("Should forward message to online Chairman via WebSocket")
        void handleMessage_shouldForwardToOnlineChairman() throws Exception {
            when(objectMapper.readValue(testMessageJson, ChatEvent.class)).thenReturn(testEvent);
            when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.of(testChairman));

            chatEventSubscriber.handleMessage(testMessageJson);

            verify(messagingTemplate).convertAndSendToUser(
                    eq("chairman"),
                    eq("/queue/sidebar"),
                    any(ChatEvent.class));
        }

        @Test
        @DisplayName("Should skip message not for Chairman")
        void handleMessage_shouldSkipIfNotForChairman() throws Exception {
            ChatEvent userEvent = ChatEvent.builder()
                    .targetUserRole("USER")
                    .targetUserId("user-123")
                    .build();
            when(objectMapper.readValue(anyString(), eq(ChatEvent.class))).thenReturn(userEvent);

            chatEventSubscriber.handleMessage(testMessageJson);

            verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should skip if Chairman not found")
        void handleMessage_shouldSkipIfChairmanNotFound() throws Exception {
            when(objectMapper.readValue(testMessageJson, ChatEvent.class)).thenReturn(testEvent);
            when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.empty());

            chatEventSubscriber.handleMessage(testMessageJson);

            verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should skip if Chairman is offline")
        void handleMessage_shouldSkipIfChairmanOffline() throws Exception {
            testChairman.setOnline(false);
            when(objectMapper.readValue(testMessageJson, ChatEvent.class)).thenReturn(testEvent);
            when(chairmanRepository.findById("chairman-123")).thenReturn(Optional.of(testChairman));

            chatEventSubscriber.handleMessage(testMessageJson);

            verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should handle JSON parsing exception")
        void handleMessage_shouldHandleJsonException() throws Exception {
            when(objectMapper.readValue(anyString(), eq(ChatEvent.class)))
                    .thenThrow(new JsonProcessingException("Parse error") {});

            // Should not throw, just log error
            chatEventSubscriber.handleMessage("invalid json");

            verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("Handle Presence")
    class HandlePresence {
        @Test
        @DisplayName("Should broadcast presence event to all")
        void handlePresence_shouldBroadcast() throws Exception {
            ChatEvent presenceEvent = ChatEvent.builder()
                    .eventType("PRESENCE_CHANGED")
                    .targetUserId("user-123")
                    .isOnline(true)
                    .build();
            when(objectMapper.readValue(anyString(), eq(ChatEvent.class))).thenReturn(presenceEvent);

            chatEventSubscriber.handlePresence(testMessageJson);

            verify(messagingTemplate).convertAndSend(eq("/topic/presence"), any(ChatEvent.class));
        }

        @Test
        @DisplayName("Should broadcast offline presence event")
        void handlePresence_shouldBroadcastOffline() throws Exception {
            ChatEvent presenceEvent = ChatEvent.builder()
                    .eventType("PRESENCE_CHANGED")
                    .targetUserId("user-123")
                    .isOnline(false)
                    .build();
            when(objectMapper.readValue(anyString(), eq(ChatEvent.class))).thenReturn(presenceEvent);

            chatEventSubscriber.handlePresence(testMessageJson);

            verify(messagingTemplate).convertAndSend(eq("/topic/presence"), any(ChatEvent.class));
        }

        @Test
        @DisplayName("Should handle JSON parsing exception in presence")
        void handlePresence_shouldHandleJsonException() throws Exception {
            when(objectMapper.readValue(anyString(), eq(ChatEvent.class)))
                    .thenThrow(new JsonProcessingException("Parse error") {});

            // Should not throw, just log error
            chatEventSubscriber.handlePresence("invalid json");

            verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
        }
    }
}
