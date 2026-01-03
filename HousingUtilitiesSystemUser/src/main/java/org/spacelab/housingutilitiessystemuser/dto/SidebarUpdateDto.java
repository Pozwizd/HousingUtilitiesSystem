package org.spacelab.housingutilitiessystemuser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SidebarUpdateDto {
    private String conversationId;
    private String lastMessagePreview;
    private Instant lastMessageAt;
    private Integer unreadCount;
}
