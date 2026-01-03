package org.spacelab.housingutilitiessystemadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;

/**
 * Сообщение в чате.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chatMessage")
public class ChatMessage {
    @Id
    private String id;

    @DocumentReference(lazy = true)
    private Conversation conversation;

    private String content;

    /**
     * ID отправителя сообщения (Chairman ID или User ID)
     */
    private String senderId;

    /**
     * Тип отправителя: "CHAIRMAN" или "USER"
     */
    private String senderType;

    private MessageDeliveryStatus deliveryStatuses;

    @CreatedDate
    private Instant createdAt;
}
