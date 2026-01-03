package org.spacelab.housingutilitiessystemchairman.service.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.models.chat.event.ChatEvent;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;

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
                .targetUserId("user-123")
                .targetUserRole("USER")
                .conversationId("conv-123")
                .messageId("msg-123")
                .senderId("chairman-123")
                .senderName("Chairman Test")
                .senderType("CHAIRMAN")
                .lastMessage("Hello!")
                .timestamp(Instant.now())
                .isOnline(true)
                .build();
    }

    @Nested
    @DisplayName("Publish Message Event")
    class PublishMessageEvent {
        @Test
        @DisplayName("Should publish message event successfully")
        void publishMessageEvent_shouldPublish() {
            when(redisTemplate.convertAndSend(eq("chat_events"), any())).thenReturn(1L);

            chatEventPublisher.publishMessageEvent(testEvent);

            verify(redisTemplate).convertAndSend("chat_events", testEvent);
        }

        @Test
        @DisplayName("Should handle exception when publishing message event")
        void publishMessageEvent_shouldHandleException() {
            when(redisTemplate.convertAndSend(eq("chat_events"), any()))
                    .thenThrow(new RuntimeException("Redis error"));

            // Should not throw exception, just log error
            chatEventPublisher.publishMessageEvent(testEvent);

            verify(redisTemplate).convertAndSend("chat_events", testEvent);
        }
    }

    @Nested
    @DisplayName("Publish Presence Event")
    class PublishPresenceEvent {
        @Test
        @DisplayName("Should publish presence event successfully")
        void publishPresenceEvent_shouldPublish() {
            when(redisTemplate.convertAndSend(eq("presence_events"), any())).thenReturn(1L);

            chatEventPublisher.publishPresenceEvent(testEvent);

            verify(redisTemplate).convertAndSend("presence_events", testEvent);
        }

        @Test
        @DisplayName("Should publish offline presence event")
        void publishPresenceEvent_shouldPublishOffline() {
            ChatEvent offlineEvent = ChatEvent.builder()
                    .eventType("PRESENCE_CHANGED")
                    .targetUserId("user-123")
                    .isOnline(false)
                    .build();

            when(redisTemplate.convertAndSend(eq("presence_events"), any())).thenReturn(1L);

            chatEventPublisher.publishPresenceEvent(offlineEvent);

            verify(redisTemplate).convertAndSend("presence_events", offlineEvent);
        }

        @Test
        @DisplayName("Should handle exception when publishing presence event")
        void publishPresenceEvent_shouldHandleException() {
            when(redisTemplate.convertAndSend(eq("presence_events"), any()))
                    .thenThrow(new RuntimeException("Redis error"));

            // Should not throw exception, just log error
            chatEventPublisher.publishPresenceEvent(testEvent);

            verify(redisTemplate).convertAndSend("presence_events", testEvent);
        }
    }
}
