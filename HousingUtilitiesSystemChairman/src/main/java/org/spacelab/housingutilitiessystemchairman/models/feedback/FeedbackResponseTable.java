package org.spacelab.housingutilitiessystemchairman.models.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponseTable {
    private String id;
    private String senderName;
    private String apartmentNumber;
    private String phone;
    private String subject;
    private LocalDateTime createdAt;
}
