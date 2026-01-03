package org.spacelab.housingutilitiessystemchairman.models.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatContactResponse {
    String id;
    String name;
    String avatar;
    boolean online;
    String participantType;
}
