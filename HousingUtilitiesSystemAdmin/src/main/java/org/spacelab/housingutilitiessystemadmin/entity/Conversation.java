package org.spacelab.housingutilitiessystemadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;
import java.util.Set;

/**
 * Диалог в чате.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversation")
public class Conversation {
    @Id
    private String id;

    @Indexed
    @DocumentReference(lazy = true)
    private Set<ChatMessage> messages;

    @CreatedDate
    private Instant createdAt;

    private Instant updatedAt;
}
