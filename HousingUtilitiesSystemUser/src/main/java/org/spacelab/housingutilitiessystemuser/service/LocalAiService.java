package org.spacelab.housingutilitiessystemuser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Local implementation of AI service for development.
 * Active in 'dev' profile.
 */
@Service
@Profile("mock")
@Slf4j
public class LocalAiService implements AiService {

    @Async
    @Override
    public CompletableFuture<String> getAiResponseAsync(String userMessage) {
        return CompletableFuture.completedFuture(getAiResponse(userMessage));
    }

    @Override
    public String getAiResponse(String userMessage) {
        log.info("🤖 [LOCAL] Submitting message to Local AI: {}", userMessage);
        // Emulate delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Это локальный ответ AI для разработки. В продакшене здесь будет ответ от настоящего AI." +
                "\nВаш запрос: " + userMessage;
    }

    @Override
    public String getAiResponseWithHistory(java.util.List<java.util.Map<String, String>> messages,
            String contextSummary) {
        log.info("🤖 [LOCAL] Getting AI response with {} messages", messages.size());
        String lastMessage = messages.isEmpty() ? "" : messages.get(messages.size() - 1).get("content");
        return getAiResponse(lastMessage);
    }

    @Override
    public String summarizeContext(java.util.List<java.util.Map<String, String>> messages) {
        log.info("🤖 [LOCAL] Summarizing {} messages", messages.size());
        return "Локальное суммирование: обсуждение содержит " + messages.size() + " сообщений.";
    }
}
