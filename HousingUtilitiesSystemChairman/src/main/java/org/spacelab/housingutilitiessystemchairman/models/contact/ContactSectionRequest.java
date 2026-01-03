package org.spacelab.housingutilitiessystemchairman.models.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactSectionRequest {
    private String id;
    private String title;
    private String content;
    private List<ContactRequest> contacts;
}
