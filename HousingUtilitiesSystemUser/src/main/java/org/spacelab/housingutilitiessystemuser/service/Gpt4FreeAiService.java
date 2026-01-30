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
@Profile({ "prod", "docker", "default" })
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
            Map<String, Object> requestBody = Map.of(
                    "provider", aiConfig.getProvider(),
                    "messages", List.of(
                            Map.of("role", "user", "content", userMessage)));

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
