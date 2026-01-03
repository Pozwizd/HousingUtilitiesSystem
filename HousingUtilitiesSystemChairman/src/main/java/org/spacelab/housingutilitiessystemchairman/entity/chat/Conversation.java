package org.spacelab.housingutilitiessystemchairman.entity.chat;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @JsonManagedReference
    private Set<ChatMessage> messages;
    @CreatedDate
    private Instant createdAt;
    private Instant updatedAt;
}
