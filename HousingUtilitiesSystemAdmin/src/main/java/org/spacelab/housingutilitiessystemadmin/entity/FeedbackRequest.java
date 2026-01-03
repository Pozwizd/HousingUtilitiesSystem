package org.spacelab.housingutilitiessystemadmin.entity;

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

    private String subject; // Тема сообщения
    private String message; // Текст сообщения
    private LocalDateTime createdAt; // Дата создания

    @DocumentReference(lazy = true)
    private User user; // Связь с пользователем

    private String userId; // ID пользователя (для запросов)
}
