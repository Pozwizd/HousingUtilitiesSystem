package org.spacelab.housingutilitiessystemchairman.entity.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spacelab.housingutilitiessystemchairman.entity.Role;

import java.time.Instant;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationParticipant {
    private String userId;
    private String fullName;
    private String avatarUrl;
    private Role role;
    private boolean online;
    private Instant lastActiveAt;
}
