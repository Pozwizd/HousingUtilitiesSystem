package org.spacelab.housingutilitiessystemchairman.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.chat.Conversation;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatConversationRequest;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatConversationResponse;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatMessageResponse;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatSidebarResponse;
import org.spacelab.housingutilitiessystemchairman.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RequestMapping("/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatSidebarController {
    private final ChatService chatService;
    @GetMapping("/sidebar")
    @ResponseBody
    public ResponseEntity<ChatSidebarResponse> getSidebar() {
        log.debug("GET /chat/sidebar");
        return ResponseEntity.ok(chatService.getChatSidebar());
    }
    @GetMapping("/conversation/{id}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable String id,
            @RequestParam(defaultValue = "50") int limit) {
        log.debug("GET /chat/conversation/{}/messages?limit={}", id, limit);
        return ResponseEntity.ok(chatService.getConversationMessages(id, limit));
    }
    @PostMapping("/conversation")
    @ResponseBody
    public ResponseEntity<ChatConversationResponse> getOrCreateConversation(
            @RequestBody ChatConversationRequest request) {
        log.debug("POST /chat/conversation for userId={}, targetType={}", request.getUserId(), request.getTargetType());
        String targetType = request.getTargetType() != null ? request.getTargetType() : "USER";
        Conversation conversation = chatService.getOrCreateConversation(request.getUserId(), targetType);
        return ResponseEntity.ok(chatService.getConversationInfo(conversation.getId()));
    }
    @GetMapping("/conversation/{id}")
    @ResponseBody
    public ResponseEntity<ChatConversationResponse> getConversation(@PathVariable String id) {
        log.debug("GET /chat/conversation/{}", id);
        return ResponseEntity.ok(chatService.getConversationInfo(id));
    }
}
