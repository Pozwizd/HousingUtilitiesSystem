package org.spacelab.housingutilitiessystemuser.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.config.AiConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for communicating with Perplexity AI via GPT4Free API.
 * Active in production or docker environments.
 */
@Service
@Profile({ "prod", "docker", "default", "dev" })
@RequiredArgsConstructor
@Slf4j
public class Gpt4FreeAiService implements AiService {

    private final RestTemplate aiRestTemplate;
    private final AiConfig aiConfig;

    /**
     * Get AI response for user message asynchronously.
     */
    @Async
    @Override
    public CompletableFuture<String> getAiResponseAsync(String userMessage) {
        return CompletableFuture.completedFuture(getAiResponse(userMessage));
    }

    /**
     * Get AI response for user message synchronously.
     */
    @Override
    public String getAiResponse(String userMessage) {
        log.info("🤖 Sending message to Perplexity AI: {}", userMessage);

        try {
            String url = aiConfig.getGpt4freeUrl() + "/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build request body
            Map<String, Object> requestMap = new java.util.HashMap<>();
            if (aiConfig.getProvider() != null && !aiConfig.getProvider().isEmpty()) {
                requestMap.put("provider", aiConfig.getProvider());
            }
            requestMap.put("model", aiConfig.getModel());
            requestMap.put("messages", List.of(Map.of("role", "user", "content", userMessage)));

            Map<String, Object> requestBody = requestMap;

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.debug("🤖 AI Request URL: {}", url);
            log.debug("🤖 AI Request body: {}", requestBody);

            ResponseEntity<AiResponse> response = aiRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    AiResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AiResponse aiResponse = response.getBody();
                if (aiResponse.getChoices() != null && !aiResponse.getChoices().isEmpty()) {
                    String content = aiResponse.getChoices().get(0).getMessage().getContent();
                    log.info("🤖 AI Response received: {}", content.substring(0, Math.min(100, content.length())));
                    return content;
                }
            }

            log.warn("🤖 AI returned empty response");
            return "Извините, я не смог обработать ваш запрос. Попробуйте ещё раз.";

        } catch (Exception e) {
            log.error("🤖 Error getting AI response: {}", e.getMessage(), e);
            return "Произошла ошибка при обращении к AI. Пожалуйста, попробуйте позже.";
        }
    }

    /**
     * Get AI response with conversation history.
     */
    @Override
    public String getAiResponseWithHistory(List<Map<String, String>> messages, String contextSummary) {
        log.info("🤖 Sending message with history ({} messages) to AI", messages.size());

        try {
            String url = aiConfig.getGpt4freeUrl() + "/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build messages list with optional context summary
            List<Map<String, String>> fullMessages = new java.util.ArrayList<>();

            // Add system message with context summary if exists
            if (contextSummary != null && !contextSummary.isEmpty()) {
                fullMessages.add(Map.of(
                        "role", "system",
                        "content", "Предыдущий контекст разговора: " + contextSummary));
            }

            // Add conversation history
            fullMessages.addAll(messages);

            Map<String, Object> requestMap = new java.util.HashMap<>();
            if (aiConfig.getProvider() != null && !aiConfig.getProvider().isEmpty()) {
                requestMap.put("provider", aiConfig.getProvider());
            }
            requestMap.put("model", aiConfig.getModel());
            requestMap.put("messages", fullMessages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestMap, headers);

            log.debug("🤖 AI Request with {} messages", fullMessages.size());

            ResponseEntity<AiResponse> response = aiRestTemplate.exchange(
                    url, HttpMethod.POST, request, AiResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AiResponse aiResponse = response.getBody();
                if (aiResponse.getChoices() != null && !aiResponse.getChoices().isEmpty()) {
                    String content = aiResponse.getChoices().get(0).getMessage().getContent();
                    log.info("🤖 AI Response received: {}", content.substring(0, Math.min(100, content.length())));
                    return content;
                }
            }

            return "Извините, я не смог обработать ваш запрос. Попробуйте ещё раз.";

        } catch (Exception e) {
            log.error("🤖 Error getting AI response with history: {}", e.getMessage(), e);
            return "Произошла ошибка при обращении к AI. Пожалуйста, попробуйте позже.";
        }
    }

    /**
     * Summarize conversation history.
     */
    @Override
    public String summarizeContext(List<Map<String, String>> messages) {
        log.info("🤖 Summarizing conversation context ({} messages)", messages.size());

        try {
            String url = aiConfig.getGpt4freeUrl() + "/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build summarization request
            List<Map<String, String>> summaryMessages = new java.util.ArrayList<>();

            // System prompt for summarization
            summaryMessages.add(Map.of(
                    "role", "system",
                    "content",
                    "Ты помощник для суммирования. Кратко опиши основные темы и ключевые моменты следующего диалога на русском языке. Будь максимально лаконичен (2-3 предложения)."));

            // Build conversation text
            StringBuilder conversationText = new StringBuilder("Диалог:\n");
            for (Map<String, String> msg : messages) {
                String role = "user".equals(msg.get("role")) ? "Пользователь" : "Ассистент";
                conversationText.append(role).append(": ").append(msg.get("content")).append("\n");
            }

            summaryMessages.add(Map.of("role", "user", "content", conversationText.toString()));

            Map<String, Object> requestMap = new java.util.HashMap<>();
            if (aiConfig.getProvider() != null && !aiConfig.getProvider().isEmpty()) {
                requestMap.put("provider", aiConfig.getProvider());
            }
            requestMap.put("model", aiConfig.getModel());
            requestMap.put("messages", summaryMessages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestMap, headers);

            ResponseEntity<AiResponse> response = aiRestTemplate.exchange(
                    url, HttpMethod.POST, request, AiResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AiResponse aiResponse = response.getBody();
                if (aiResponse.getChoices() != null && !aiResponse.getChoices().isEmpty()) {
                    String summary = aiResponse.getChoices().get(0).getMessage().getContent();
                    log.info("🤖 Context summary: {}", summary);
                    return summary;
                }
            }

            return null;

        } catch (Exception e) {
            log.error("🤖 Error summarizing context: {}", e.getMessage(), e);
            return null;
        }
    }

    // === Response DTOs ===

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiResponse {
        private String id;
        private String model;
        private String provider;
        private List<Choice> choices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private int index;
        private Message message;
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
    }
}
