package org.spacelab.housingutilitiessystemadmin.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Contact {
    @Id
    private String id;
    private String fullName;
    private String contactType; // "chairman" or "employee"
    private String role;
    private String phone;
    private String photoPath; // Path to uploaded photo file
    private String description;
}