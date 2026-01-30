package org.spacelab.housingutilitiessystemuser.service;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for AI service communication.
 */
public interface AiService {

    String AI_PARTICIPANT_ID = "perplexity-ai";
    String AI_PARTICIPANT_TYPE = "AI";
    String AI_NAME = "Perplexity AI";
    String AI_AVATAR = null; // Will use initial letter

    /**
     * Get AI response for user message asynchronously.
     */
    CompletableFuture<String> getAiResponseAsync(String userMessage);

    /**
     * Get AI response for user message synchronously.
     */
    String getAiResponse(String userMessage);

    /**
     * Check if participant ID is AI.
     */
    static boolean isAiParticipant(String participantId) {
        return AI_PARTICIPANT_ID.equals(participantId);
    }
}
