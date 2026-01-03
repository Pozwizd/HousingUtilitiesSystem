package org.spacelab.housingutilitiessystemadmin.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Data
@Document
public class ContactSection {
    @Id
    private String id;
    private String title;
    private String content;
    @DocumentReference
    private List<Contact> contacts;
}