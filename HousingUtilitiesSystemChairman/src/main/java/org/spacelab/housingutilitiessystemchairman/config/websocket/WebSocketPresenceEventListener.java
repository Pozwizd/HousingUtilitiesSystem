package org.spacelab.housingutilitiessystemchairman.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.models.chat.event.ChatEvent;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.UserRepository;
import org.spacelab.housingutilitiessystemchairman.service.chat.ChatEventPublisher;
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
    private final ChairmanRepository chairmanRepository;
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
        String userId = null;
        Optional<Chairman> chairmanOpt = chairmanRepository.findByLogin(login);
        if (chairmanOpt.isPresent()) {
            Chairman chairman = chairmanOpt.get();
            chairman.setOnline(isOnline);
            chairman.setLastActiveAt(now);
            chairmanRepository.save(chairman);
            userId = chairman.getId();
            log.debug("Updated Chairman {} status to {}", login, isOnline ? "ONLINE" : "OFFLINE");
        } else {
            Optional<User> userOpt = userRepository.findByLogin(login);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setOnline(isOnline);
                user.setLastActiveAt(now);
                userRepository.save(user);
                userId = user.getId();
                log.debug("Updated User {} status to {}", login, isOnline ? "ONLINE" : "OFFLINE");
            }
        }
        return userId;
    }
    private void publishPresenceEvent(String userId, String login, boolean isOnline) {
        try {
            ChatEvent event = ChatEvent.builder()
                    .eventType(isOnline ? "USER_ONLINE" : "USER_OFFLINE")
                    .targetUserId(userId)
                    .isOnline(isOnline)
                    .timestamp(Instant.now())
                    .build();
            chatEventPublisher.publishPresenceEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish presence event for user {}: {}", login, e.getMessage());
        }
    }
}