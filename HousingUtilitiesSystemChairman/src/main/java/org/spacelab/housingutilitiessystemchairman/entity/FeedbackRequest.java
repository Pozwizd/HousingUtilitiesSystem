package org.spacelab.housingutilitiessystemchairman.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "feedback_requests")
public class FeedbackRequest {
    @Id
    private String id;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
    @DocumentReference(lazy = true)
    private User user;
    private String userId;
}
