package org.spacelab.housingutilitiessystemchairman.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.chat.Conversation;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSubscriptionInterceptor implements ChannelInterceptor {
    private final ChairmanRepository chairmanRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/chat/")) {
                String conversationId = destination.replace("/topic/chat/", "");
                Principal principal = accessor.getUser();
                log.debug("🔐 Checking subscription access: user={}, destination={}, conversationId={}",
                        principal != null ? principal.getName() : "null", destination, conversationId);
                if (principal == null) {
                    log.warn("🚫 Subscription rejected: no principal");
                    throw new MessagingException("Требуется аутентификация для подписки на чат");
                }
                if (!hasAccessToConversation(principal.getName(), conversationId)) {
                    log.warn("🚫 Subscription rejected: user {} has no access to conversation {}",
                            principal.getName(), conversationId);
                    throw new MessagingException("Нет доступа к диалогу: " + conversationId);
                }
                log.info("✅ Subscription allowed: user {} -> conversation {}",
                        principal.getName(), conversationId);
            }
        }
        return message;
    }

    private boolean hasAccessToConversation(String userLogin, String conversationId) {
        Optional<Chairman> chairmanOpt = chairmanRepository.findByLogin(userLogin);
        if (chairmanOpt.isEmpty()) {
            chairmanOpt = chairmanRepository.findByEmail(userLogin);
        }
        return chairmanOpt
                .map(chairman -> {
                    Set<Conversation> conversations = chairman.getConversations();
                    if (conversations == null) {
                        return false;
                    }
                    return conversations.stream()
                            .anyMatch(c -> c.getId().equals(conversationId));
                })
                .orElse(false);
    }
}
