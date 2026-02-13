package org.spacelab.housingutilitiessystemuser.service;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for AI service communication.
 */
public interface AiService {

    String AI_PARTICIPANT_ID = "ai-assistant";
    String AI_PARTICIPANT_TYPE = "AI";
    String AI_NAME = "AI Ассистент";
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
     * Get AI response with conversation history.
     * 
     * @param messages       List of messages in format [{role: "user"|"assistant",
     *                       content: "..."}]
     * @param contextSummary Optional summary of previous context
     * @return AI response
     */
    String getAiResponseWithHistory(java.util.List<java.util.Map<String, String>> messages, String contextSummary);

    /**
     * Summarize conversation history.
     * 
     * @param messages List of messages to summarize
     * @return Concise summary of the conversation
     */
    String summarizeContext(java.util.List<java.util.Map<String, String>> messages);

    /**
     * Check if participant ID is AI.
     */
    static boolean isAiParticipant(String participantId) {
        return AI_PARTICIPANT_ID.equals(participantId);
    }
}
