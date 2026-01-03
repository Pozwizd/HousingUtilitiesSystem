package org.spacelab.housingutilitiessystemuser.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.models.chat.event.ChatEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEventPublisher {

    private static final String CHAT_EVENTS_CHANNEL = "chat_events";
    private static final String PRESENCE_EVENTS_CHANNEL = "presence_events";

    private final RedisTemplate<String, Object> redisTemplate;

    
    public void publishMessageEvent(ChatEvent event) {
        try {
            log.debug("Publishing message event to Redis: conversationId={}, targetUserId={}",
                    event.getConversationId(), event.getTargetUserId());
            redisTemplate.convertAndSend(CHAT_EVENTS_CHANNEL, event);
            log.info("✅ Message event published to Redis channel '{}' for user {}",
                    CHAT_EVENTS_CHANNEL, event.getTargetUserId());
        } catch (Exception e) {
            log.error("❌ Failed to publish message event to Redis: {}", e.getMessage(), e);
        }
    }

    
    public void publishPresenceEvent(ChatEvent event) {
        try {
            log.debug("Publishing presence event to Redis: userId={}, isOnline={}",
                    event.getTargetUserId(), event.isOnline());
            redisTemplate.convertAndSend(PRESENCE_EVENTS_CHANNEL, event);
            log.info("✅ Presence event published to Redis channel '{}': {} is now {}",
                    PRESENCE_EVENTS_CHANNEL, event.getTargetUserId(),
                    event.isOnline() ? "ONLINE" : "OFFLINE");
        } catch (Exception e) {
            log.error("❌ Failed to publish presence event to Redis: {}", e.getMessage(), e);
        }
    }
}
