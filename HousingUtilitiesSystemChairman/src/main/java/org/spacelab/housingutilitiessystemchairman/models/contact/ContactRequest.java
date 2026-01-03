package org.spacelab.housingutilitiessystemchairman.models.contact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {
    private String id;
    private String fullName;
    private String contactType;
    private String role;
    private String phone;
    private Integer photoIndex;
    private String description;
}
