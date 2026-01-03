package org.spacelab.housingutilitiessystemuser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemuser.entity.Contact;
import org.spacelab.housingutilitiessystemuser.entity.ContactSection;
import org.spacelab.housingutilitiessystemuser.models.contact.ContactSectionResponse;
import org.spacelab.housingutilitiessystemuser.repository.ContactSectionRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Tests")
class ContactServiceTest {

    @Mock
    private ContactSectionRepository contactSectionRepository;

    @InjectMocks
    private ContactService contactService;

    private ContactSection testSection;
    private Contact testContact;

    @BeforeEach
    void setUp() {
        testContact = new Contact();
        testContact.setId("contact-id");
        testContact.setFullName("John Doe");
        testContact.setContactType("employee");
        testContact.setRole("Manager");
        testContact.setPhone("1234567890");
        testContact.setPhotoPath("/photos/john.jpg");
        testContact.setDescription("Test description");

        testSection = new ContactSection();
        testSection.setId("section-id");
        testSection.setTitle("Management");
        testSection.setContent("Section content");
        testSection.setContacts(List.of(testContact));
    }

    @Nested
    @DisplayName("Get All Sections")
    class GetAllSections {
        @Test
        @DisplayName("Should return all sections with contacts")
        void getAllSections_shouldReturnSections() {
            when(contactSectionRepository.findAll()).thenReturn(List.of(testSection));

            List<ContactSectionResponse> result = contactService.getAllSections();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("section-id");
            assertThat(result.get(0).getTitle()).isEqualTo("Management");
            assertThat(result.get(0).getContacts()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no sections")
        void getAllSections_shouldReturnEmptyList() {
            when(contactSectionRepository.findAll()).thenReturn(List.of());

            List<ContactSectionResponse> result = contactService.getAllSections();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle section with null contacts")
        void getAllSections_shouldHandleNullContacts() {
            testSection.setContacts(null);
            when(contactSectionRepository.findAll()).thenReturn(List.of(testSection));

            List<ContactSectionResponse> result = contactService.getAllSections();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContacts()).isEmpty();
        }

        @Test
        @DisplayName("Should handle section with empty contacts")
        void getAllSections_shouldHandleEmptyContacts() {
            testSection.setContacts(new ArrayList<>());
            when(contactSectionRepository.findAll()).thenReturn(List.of(testSection));

            List<ContactSectionResponse> result = contactService.getAllSections();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContacts()).isEmpty();
        }
    }
}
