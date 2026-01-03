package org.spacelab.housingutilitiessystemuser.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.models.chat.event.ChatEvent;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEventSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    
    public void handleMessage(String message) {
        try {
            log.debug("Received message event from Redis: {}", message);
            ChatEvent event = objectMapper.readValue(message, ChatEvent.class);

            
            if ("CHAIRMAN".equals(event.getTargetUserRole())) {
                log.debug("Event for Chairman, skipping in User module. TargetRole: {}", event.getTargetUserRole());
                return;
            }

            
            Optional<User> userOpt = userRepository.findById(event.getTargetUserId());
            if (userOpt.isEmpty()) {
                log.warn("User not found for ID: {}", event.getTargetUserId());
                return;
            }

            User user = userOpt.get();

            
            if (!user.isOnline()) {
                log.debug("User {} is offline, skipping WebSocket notification", user.getLogin());
                return;
            }

            
            String username = user.getLogin() != null ? user.getLogin() : user.getEmail();

            
            log.info("📨 Sending sidebar update to User {} via WebSocket", username);
            messagingTemplate.convertAndSendToUser(
                    username,
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
