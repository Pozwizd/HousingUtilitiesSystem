package org.spacelab.housingutilitiessystemuser.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.models.chat.event.ChatEvent;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.spacelab.housingutilitiessystemuser.service.chat.ChatEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketPresenceEventListener {

    private final UserRepository userRepository;
    private final ChatEventPublisher chatEventPublisher;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth = (Authentication) accessor.getUser();

        if (auth != null && auth.isAuthenticated()) {
            String login = auth.getName();
            log.info("✅ USER CONNECTED: {}", login);
            String userId = updateStatus(login, true);

            
            if (userId != null) {
                publishPresenceEvent(userId, login, true);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth = (Authentication) accessor.getUser();

        if (auth != null) {
            String login = auth.getName();
            log.info("❌ USER DISCONNECTED: {}", login);
            String userId = updateStatus(login, false);

            
            if (userId != null) {
                publishPresenceEvent(userId, login, false);
            }
        }
    }

    private String updateStatus(String login, boolean isOnline) {
        Instant now = Instant.now();

        
        Optional<User> userOpt = userRepository.findByEmail(login);
        if (userOpt.isEmpty()) {
            
            userOpt = userRepository.findByLogin(login);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOnline(isOnline);
            user.setLastActiveAt(now);
            userRepository.save(user);
            log.debug("Updated User {} status to {}", login, isOnline ? "ONLINE" : "OFFLINE");
            return user.getId();
        }

        log.warn("User not found for login: {}", login);
        return null;
    }

    
    private void publishPresenceEvent(String userId, String login, boolean isOnline) {
        try {
            ChatEvent event = ChatEvent.builder()
                    .eventType(isOnline ? "USER_ONLINE" : "USER_OFFLINE")
                    .targetUserId(userId)
                    .targetUserRole("USER")
                    .isOnline(isOnline)
                    .timestamp(Instant.now())
                    .build();

            chatEventPublisher.publishPresenceEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish presence event for user {}: {}", login, e.getMessage());
        }
    }
}
