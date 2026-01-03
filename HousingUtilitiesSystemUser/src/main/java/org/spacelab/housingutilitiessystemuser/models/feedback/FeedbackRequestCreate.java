package org.spacelab.housingutilitiessystemuser.models.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequestCreate {
    private String subject;
    private String message;
}
