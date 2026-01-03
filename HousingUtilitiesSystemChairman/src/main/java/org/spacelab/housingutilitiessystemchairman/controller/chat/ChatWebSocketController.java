package org.spacelab.housingutilitiessystemchairman.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.exception.OperationException;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatMessageRequest;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatMessageResponse;
import org.spacelab.housingutilitiessystemchairman.service.ChairmanService;
import org.spacelab.housingutilitiessystemchairman.service.chat.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
        private final ChatService chatService;
        private final ChairmanService chairmanService;
        @MessageMapping("/chat/{conversationId}/sendMessage")
        @SendTo("/topic/chat/{conversationId}")
        public ChatMessageResponse sendMessage(
                        @DestinationVariable String conversationId,
                        @Payload ChatMessageRequest request,
                        Principal principal) {
                log.info("📨 WebSocket message received for conversation {}: {}",
                                conversationId, request.getContent());
                String login = principal != null ? principal.getName() : null;
                if (login == null) {
                        log.error("🚫 WebSocket: Principal is null");
                        throw new OperationException("отправке сообщения", "Не удалось определить отправителя",
                                        HttpStatus.UNAUTHORIZED);
                }
                Chairman sender = chairmanService.findByEmail(login)
                                .or(() -> chairmanService.findByLogin(login))
                                .orElseThrow(() -> new OperationException("отправке сообщения",
                                                "Пользователь не найден: " + login, HttpStatus.UNAUTHORIZED));
                log.info("👤 Sender identified: {}", sender.getLogin());
                ChatMessageResponse response = chatService.sendMessage(
                                conversationId,
                                request.getContent(),
                                sender);
                log.info("✅ Message processed and broadcasting to /topic/chat/{}", conversationId);
                return response;
        }
}
