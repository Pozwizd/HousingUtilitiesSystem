package org.spacelab.housingutilitiessystemuser.service;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemuser.entity.Contact;
import org.spacelab.housingutilitiessystemuser.entity.ContactSection;
import org.spacelab.housingutilitiessystemuser.models.contact.ContactResponse;
import org.spacelab.housingutilitiessystemuser.models.contact.ContactSectionResponse;
import org.spacelab.housingutilitiessystemuser.repository.ContactSectionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactSectionRepository contactSectionRepository;

    public List<ContactSectionResponse> getAllSections() {
        List<ContactSection> sections = contactSectionRepository.findAll();
        return sections.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ContactSectionResponse toResponse(ContactSection section) {
        List<ContactResponse> contacts = section.getContacts() != null
                ? section.getContacts().stream()
                        .map(this::toContactResponse)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return ContactSectionResponse.builder()
                .id(section.getId())
                .title(section.getTitle())
                .content(section.getContent())
                .contacts(contacts)
                .build();
    }

    private ContactResponse toContactResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .fullName(contact.getFullName())
                .contactType(contact.getContactType())
                .role(contact.getRole())
                .phone(contact.getPhone())
                .photoPath(contact.getPhotoPath())
                .description(contact.getDescription())
                .build();
    }
}
