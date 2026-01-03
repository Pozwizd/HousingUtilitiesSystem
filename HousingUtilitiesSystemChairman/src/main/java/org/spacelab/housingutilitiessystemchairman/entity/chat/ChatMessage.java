package org.spacelab.housingutilitiessystemchairman.entity.chat;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ChatMessage {
    @Id
    private String id;
    @DocumentReference(lazy = true)
    @JsonManagedReference
    private Conversation conversation;
    private String content;
    private String senderId;
    private String senderType;
    private MessageDeliveryStatus deliveryStatuses;
    @CreatedDate
    private Instant createdAt;
}
