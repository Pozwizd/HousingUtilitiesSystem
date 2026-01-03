package org.spacelab.housingutilitiessystemuser.models.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequestResponse {
    private String id;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
}
