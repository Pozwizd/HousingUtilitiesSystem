package org.spacelab.housingutilitiessystemuser.controller.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.exception.OperationException;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatMessageResponse;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatSendMessageRequest;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.spacelab.housingutilitiessystemuser.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    
    @MessageMapping("/chat/{conversationId}/sendMessage")
    @SendTo("/topic/chat/{conversationId}")
    public ChatMessageResponse sendMessage(
            @DestinationVariable String conversationId,
            @Payload ChatSendMessageRequest request,
            Principal principal) {

        log.info("📨 WebSocket message received for conversation {}: {}",
                conversationId, request.getContent());

        
        String login = principal != null ? principal.getName() : null;
        if (login == null) {
            log.error("🚫 WebSocket: Principal is null");
            throw new OperationException("отправке сообщения", "Не удалось определить отправителя",
                    HttpStatus.UNAUTHORIZED);
        }

        User sender = userRepository.findByEmail(login)
                .orElseThrow(() -> new OperationException("отправке сообщения",
                        "Пользователь не найден: " + login, HttpStatus.UNAUTHORIZED));

        log.info("👤 Sender identified: {}", sender.getEmail());

        
        ChatMessageResponse response = chatService.sendMessage(
                conversationId,
                request.getContent(),
                sender);

        log.info("✅ Message processed and broadcasting to /topic/chat/{}", conversationId);

        return response;
    }
}
