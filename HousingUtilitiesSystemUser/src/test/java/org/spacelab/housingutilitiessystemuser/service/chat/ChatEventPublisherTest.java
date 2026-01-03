package org.spacelab.housingutilitiessystemuser.service.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemuser.models.chat.event.ChatEvent;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatEventPublisher Tests")
class ChatEventPublisherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private ChatEventPublisher chatEventPublisher;

    private ChatEvent testEvent;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Publish Message Event")
    class PublishMessageEvent {
        @Test
        @DisplayName("Should publish message event to Redis")
        void publishMessageEvent_shouldPublish() {
            when(redisTemplate.convertAndSend(eq("chat_events"), any())).thenReturn(1L);

            chatEventPublisher.publishMessageEvent(testEvent);

            verify(redisTemplate).convertAndSend(eq("chat_events"), eq(testEvent));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void publishMessageEvent_shouldHandleException() {
            when(redisTemplate.convertAndSend(any(), any())).thenThrow(new RuntimeException("Redis error"));

            // Should not throw, just log error
            chatEventPublisher.publishMessageEvent(testEvent);

            verify(redisTemplate).convertAndSend(eq("chat_events"), eq(testEvent));
        }
    }

    @Nested
    @DisplayName("Publish Presence Event")
    class PublishPresenceEvent {
        @Test
        @DisplayName("Should publish presence event to Redis")
        void publishPresenceEvent_shouldPublish() {
            when(redisTemplate.convertAndSend(eq("presence_events"), any())).thenReturn(1L);

            chatEventPublisher.publishPresenceEvent(testEvent);

            verify(redisTemplate).convertAndSend(eq("presence_events"), eq(testEvent));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void publishPresenceEvent_shouldHandleException() {
            when(redisTemplate.convertAndSend(any(), any())).thenThrow(new RuntimeException("Redis error"));

            // Should not throw, just log error
            chatEventPublisher.publishPresenceEvent(testEvent);

            verify(redisTemplate).convertAndSend(eq("presence_events"), eq(testEvent));
        }

        @Test
        @DisplayName("Should publish online status")
        void publishPresenceEvent_shouldPublishOnlineStatus() {
            testEvent = ChatEvent.builder()
                    .eventType("PRESENCE")
                    .targetUserId("user-123")
                    .isOnline(true)
                    .build();
            when(redisTemplate.convertAndSend(eq("presence_events"), any())).thenReturn(1L);

            chatEventPublisher.publishPresenceEvent(testEvent);

            verify(redisTemplate).convertAndSend(eq("presence_events"), eq(testEvent));
        }

        @Test
        @DisplayName("Should publish offline status")
        void publishPresenceEvent_shouldPublishOfflineStatus() {
            testEvent = ChatEvent.builder()
                    .eventType("PRESENCE")
                    .targetUserId("user-123")
                    .isOnline(false)
                    .build();
            when(redisTemplate.convertAndSend(eq("presence_events"), any())).thenReturn(1L);

            chatEventPublisher.publishPresenceEvent(testEvent);

            verify(redisTemplate).convertAndSend(eq("presence_events"), eq(testEvent));
        }
    }
}
