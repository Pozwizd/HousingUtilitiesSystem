package org.spacelab.housingutilitiessystemchairman.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.models.chat.event.ChatEvent;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEventSubscriber {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChairmanRepository chairmanRepository;
    private final ObjectMapper objectMapper;
    public void handleMessage(String message) {
        try {
            log.debug("Received message event from Redis: {}", message);
            ChatEvent event = objectMapper.readValue(message, ChatEvent.class);
            if (!"CHAIRMAN".equals(event.getTargetUserRole())) {
                log.debug("Event not for Chairman, skipping. TargetRole: {}", event.getTargetUserRole());
                return;
            }
            Optional<Chairman> chairmanOpt = chairmanRepository.findById(event.getTargetUserId());
            if (chairmanOpt.isEmpty()) {
                log.warn("Chairman not found for ID: {}", event.getTargetUserId());
                return;
            }
            Chairman chairman = chairmanOpt.get();
            if (!chairman.isOnline()) {
                log.debug("Chairman {} is offline, skipping WebSocket notification", chairman.getLogin());
                return;
            }
            log.info("📨 Sending sidebar update to Chairman {} via WebSocket", chairman.getLogin());
            messagingTemplate.convertAndSendToUser(
                    chairman.getLogin(),
                    "/queue/sidebar",
                    event);
        } catch (Exception e) {
            log.error("❌ Failed to handle message event from Redis: {}", e.getMessage(), e);
        }
    }
    public void handlePresence(String message) {
        try {
            log.debug("Received presence event from Redis: {}", message);
            ChatEvent event = objectMapper.readValue(message, ChatEvent.class);
            messagingTemplate.convertAndSend("/topic/presence", event);
            log.info("📡 Broadcasted presence event: userId={} is now {}",
                    event.getTargetUserId(), event.isOnline() ? "ONLINE" : "OFFLINE");
        } catch (Exception e) {
            log.error("❌ Failed to handle presence event from Redis: {}", e.getMessage(), e);
        }
    }
}
