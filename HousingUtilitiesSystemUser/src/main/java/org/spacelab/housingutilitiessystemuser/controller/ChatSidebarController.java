package org.spacelab.housingutilitiessystemuser.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.chat.Conversation;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatConversationResponse;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatMessageResponse;
import org.spacelab.housingutilitiessystemuser.models.chat.ChatSidebarResponse;
import org.spacelab.housingutilitiessystemuser.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatSidebarController {

    private final ChatService chatService;

    
    @GetMapping("/chat/sidebar")
    @ResponseBody
    public ResponseEntity<ChatSidebarResponse> getSidebar() {
        log.info("📥 GET /chat/sidebar");
        ChatSidebarResponse sidebar = chatService.getChatSidebar();
        return ResponseEntity.ok(sidebar);
    }

    
    @GetMapping("/chat/conversation/{id}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable String id,
            @RequestParam(defaultValue = "50") int limit) {
        log.info("📥 GET /chat/conversation/{}/messages?limit={}", id, limit);
        List<ChatMessageResponse> messages = chatService.getConversationMessages(id, limit);
        return ResponseEntity.ok(messages);
    }

    
    @PostMapping("/chat/conversation")
    @ResponseBody
    public ResponseEntity<ChatConversationResponse> getOrCreateConversation(
            @RequestBody Map<String, String> request) {
        String targetId = request.get("userId") != null ? request.get("userId") : request.get("targetId");
        String targetType = request.get("targetType") != null ? request.get("targetType") : "CHAIRMAN";
        log.info("📥 POST /chat/conversation with targetId: {}, targetType: {}", targetId, targetType);

        Conversation conversation = chatService.getOrCreateConversation(targetId, targetType);
        ChatConversationResponse response = chatService.getConversationInfo(conversation.getId());
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/chat/conversation/{id}")
    @ResponseBody
    public ResponseEntity<ChatConversationResponse> getConversationInfo(@PathVariable String id) {
        log.info("📥 GET /chat/conversation/{}", id);
        ChatConversationResponse info = chatService.getConversationInfo(id);
        return ResponseEntity.ok(info);
    }
}
