package org.spacelab.housingutilitiessystemchairman.config.websocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null) {
                    StompCommand command = accessor.getCommand();
                    if (command == StompCommand.CONNECT) {
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth != null && auth.isAuthenticated() &&
                                !(auth instanceof AnonymousAuthenticationToken)) {
                            accessor.setUser(auth);
                            log.info("✅ WebSocket CONNECT authenticated: {}", auth.getName());
                        } else {
                            log.debug("WebSocket CONNECT - using session authentication");
                        }
                    } else if (command == StompCommand.SUBSCRIBE) {
                        log.debug("WebSocket SUBSCRIBE: {}", accessor.getDestination());
                    } else if (command == StompCommand.SEND) {
                        log.debug("WebSocket SEND: {}", accessor.getDestination());
                    }
                }
                return message;
            }
        });
    }
}
