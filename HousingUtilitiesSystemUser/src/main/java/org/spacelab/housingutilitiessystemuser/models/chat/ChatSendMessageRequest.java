package org.spacelab.housingutilitiessystemuser.models.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSendMessageRequest {

    @NotBlank
    private String content;
}
