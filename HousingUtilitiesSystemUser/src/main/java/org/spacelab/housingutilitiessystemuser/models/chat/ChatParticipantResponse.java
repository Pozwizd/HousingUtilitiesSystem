package org.spacelab.housingutilitiessystemuser.models.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spacelab.housingutilitiessystemuser.entity.Role;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantResponse {
    private String userId;
    private String fullName;
    private String avatarUrl;
    private Role role;
    private boolean online;
    private Instant lastActiveAt;
}
