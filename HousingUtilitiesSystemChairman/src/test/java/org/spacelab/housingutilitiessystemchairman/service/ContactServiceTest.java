package org.spacelab.housingutilitiessystemchairman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.Contact;
import org.spacelab.housingutilitiessystemchairman.entity.ContactSection;
import org.spacelab.housingutilitiessystemchairman.mappers.ContactMapper;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactRequest;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactResponse;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactSectionRequest;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactSectionResponse;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ContactRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ContactSectionRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Tests")
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactSectionRepository contactSectionRepository;

    @Mock
    private ContactMapper contactMapper;

    @Mock
    private FileService fileService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ContactService contactService;

    private Contact testContact;
    private ContactSection testSection;
    private ContactResponse testContactResponse;
    private ContactSectionResponse testSectionResponse;

    @BeforeEach
    void setUp() {
        testContact = new Contact();
        testContact.setId("contact-id");
        testContact.setFullName("Test Contact");
        testContact.setPhone("1234567890");

        testSection = new ContactSection();
        testSection.setId("section-id");
        testSection.setTitle("Test Section");
        testSection.setContacts(new ArrayList<>());

        testContactResponse = new ContactResponse();
        testContactResponse.setId("contact-id");

        testSectionResponse = new ContactSectionResponse();
        testSectionResponse.setId("section-id");
    }

    @Nested
    @DisplayName("Contact CRUD Operations")
    class ContactCrudOperations {
        @Test
        @DisplayName("Should save contact")
        void save_shouldSaveContact() {
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            Contact result = contactService.save(testContact);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnContact() {
            when(contactRepository.findById("contact-id")).thenReturn(Optional.of(testContact));
            Optional<Contact> result = contactService.findById("contact-id");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should find all contacts")
        void findAll_shouldReturnAllContacts() {
            when(contactRepository.findAll()).thenReturn(Arrays.asList(testContact, new Contact()));
            List<Contact> result = contactService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete by id")
        void deleteById_shouldDeleteContact() {
            doNothing().when(contactRepository).deleteById("contact-id");
            contactService.deleteById("contact-id");
            verify(contactRepository).deleteById("contact-id");
        }

        @Test
        @DisplayName("Should update contact")
        void update_shouldUpdateContact() {
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            Contact result = contactService.update(testContact);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should save all contacts")
        void saveAll_shouldSaveAllContacts() {
            List<Contact> contacts = Arrays.asList(testContact, new Contact());
            when(contactRepository.saveAll(contacts)).thenReturn(contacts);
            List<Contact> result = contactService.saveAll(contacts);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllContacts() {
            doNothing().when(contactRepository).deleteAll();
            contactService.deleteAll();
            verify(contactRepository).deleteAll();
        }
    }

    @Nested
    @DisplayName("Get Contact By Id")
    class GetContactById {
        @Test
        @DisplayName("Should get contact by id")
        void getContactById_shouldReturnResponse() {
            when(contactRepository.findById("contact-id")).thenReturn(Optional.of(testContact));
            when(contactMapper.toResponse(testContact)).thenReturn(testContactResponse);

            ContactResponse result = contactService.getContactById("contact-id");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getContactById_shouldThrowException() {
            when(contactRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.getContactById("unknown"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Get All Contacts")
    class GetAllContacts {
        @Test
        @DisplayName("Should get all contacts as responses")
        void getAllContacts_shouldReturnResponses() {
            when(contactRepository.findAll()).thenReturn(List.of(testContact));
            when(contactMapper.toResponseList(any())).thenReturn(List.of(testContactResponse));

            List<ContactResponse> result = contactService.getAllContacts();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Create Contact")
    class CreateContact {
        @Test
        @DisplayName("Should create contact")
        void createContact_shouldCreate() throws IOException {
            ContactRequest request = new ContactRequest();
            when(contactMapper.toEntity(request)).thenReturn(testContact);
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactMapper.toResponse(testContact)).thenReturn(testContactResponse);

            ContactResponse result = contactService.createContact(request, null);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should create contact with photo")
        void createContact_withPhoto_shouldUploadPhoto() throws IOException {
            ContactRequest request = new ContactRequest();
            when(contactMapper.toEntity(request)).thenReturn(testContact);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(fileService.uploadFile(multipartFile)).thenReturn("uploads/photo.jpg");
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactMapper.toResponse(testContact)).thenReturn(testContactResponse);

            contactService.createContact(request, multipartFile);

            verify(fileService).uploadFile(multipartFile);
        }
    }

    @Nested
    @DisplayName("Update Contact")
    class UpdateContact {
        @Test
        @DisplayName("Should update contact")
        void updateContact_shouldUpdate() throws IOException {
            ContactRequest request = new ContactRequest();
            when(contactRepository.findById("contact-id")).thenReturn(Optional.of(testContact));
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactMapper.toResponse(testContact)).thenReturn(testContactResponse);

            ContactResponse result = contactService.updateContact("contact-id", request, null);

            assertThat(result).isNotNull();
            verify(contactMapper).partialUpdate(request, testContact);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void updateContact_shouldThrowException() {
            ContactRequest request = new ContactRequest();
            when(contactRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.updateContact("unknown", request, null))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Section Operations")
    class SectionOperations {
        @Test
        @DisplayName("Should get all sections")
        void getAllSections_shouldReturnSections() {
            when(contactSectionRepository.findAll()).thenReturn(List.of(testSection));
            when(contactMapper.toSectionResponseList(any())).thenReturn(List.of(testSectionResponse));

            List<ContactSectionResponse> result = contactService.getAllSections();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get section by id")
        void getSectionById_shouldReturnSection() {
            when(contactSectionRepository.findById("section-id")).thenReturn(Optional.of(testSection));
            when(contactMapper.toSectionResponse(testSection)).thenReturn(testSectionResponse);

            ContactSectionResponse result = contactService.getSectionById("section-id");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when section not found")
        void getSectionById_shouldThrowException() {
            when(contactSectionRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.getSectionById("unknown"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Create Section")
    class CreateSection {
        @Test
        @DisplayName("Should create section without contacts")
        void createSection_shouldCreate() throws IOException {
            ContactSectionRequest request = new ContactSectionRequest();
            request.setContacts(null); // No contacts

            when(contactMapper.toSectionEntity(request)).thenReturn(testSection);
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            ContactSectionResponse result = contactService.createSection(request, null);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should create section with contacts")
        void createSection_withContacts_shouldCreate() throws IOException {
            ContactSectionRequest request = new ContactSectionRequest();
            ContactRequest contactRequest = new ContactRequest();
            request.setContacts(List.of(contactRequest));

            when(contactMapper.toSectionEntity(request)).thenReturn(testSection);
            when(contactMapper.toEntity(any(ContactRequest.class))).thenReturn(testContact);
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            ContactSectionResponse result = contactService.createSection(request, null);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should create section with contacts and photos")
        void createSection_withContactsAndPhotos_shouldUploadPhotos() throws IOException {
            ContactSectionRequest request = new ContactSectionRequest();
            ContactRequest contactRequest = new ContactRequest();
            contactRequest.setPhotoIndex(0);
            request.setContacts(List.of(contactRequest));

            when(contactMapper.toSectionEntity(request)).thenReturn(testSection);
            when(contactMapper.toEntity(any(ContactRequest.class))).thenReturn(testContact);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(fileService.uploadFile(multipartFile)).thenReturn("uploads/photo.jpg");
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            ContactSectionResponse result = contactService.createSection(request, List.of(multipartFile));

            assertThat(result).isNotNull();
            verify(fileService).uploadFile(multipartFile);
        }

        @Test
        @DisplayName("Should handle empty contacts list")
        void createSection_withEmptyContacts_shouldSetEmptyList() throws IOException {
            ContactSectionRequest request = new ContactSectionRequest();
            request.setContacts(new ArrayList<>());

            when(contactMapper.toSectionEntity(request)).thenReturn(testSection);
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            ContactSectionResponse result = contactService.createSection(request, null);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Update Section")
    class UpdateSection {
        @Test
        @DisplayName("Should update section with new contacts")
        void updateSection_shouldUpdateWithNewContacts() throws IOException {
            ContactSectionRequest request = new ContactSectionRequest();
            ContactRequest contactRequest = new ContactRequest(); // No ID - new contact
            request.setContacts(List.of(contactRequest));

            testSection.setContacts(new ArrayList<>());

            when(contactSectionRepository.findById("section-id")).thenReturn(Optional.of(testSection));
            when(contactMapper.toEntity(any(ContactRequest.class))).thenReturn(testContact);
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            ContactSectionResponse result = contactService.updateSection("section-id", request, null);

            assertThat(result).isNotNull();
            verify(contactRepository).save(any(Contact.class));
        }

        @Test
        @DisplayName("Should update existing contact in section")
        void updateSection_shouldUpdateExistingContact() throws IOException {
            ContactSectionRequest request = new ContactSectionRequest();
            ContactRequest contactRequest = new ContactRequest();
            contactRequest.setId("contact-id");
            request.setContacts(List.of(contactRequest));

            testSection.setContacts(new ArrayList<>(List.of(testContact)));

            when(contactSectionRepository.findById("section-id")).thenReturn(Optional.of(testSection));
            when(contactRepository.findById("contact-id")).thenReturn(Optional.of(testContact));
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            ContactSectionResponse result = contactService.updateSection("section-id", request, null);

            assertThat(result).isNotNull();
            verify(contactMapper).partialUpdate(any(ContactRequest.class), any(Contact.class));
        }

        @Test
        @DisplayName("Should delete removed contacts")
        void updateSection_shouldDeleteRemovedContacts() throws IOException {
            Contact otherContact = new Contact();
            otherContact.setId("other-contact");
            otherContact.setPhotoPath("old-photo.jpg");

            ContactSectionRequest request = new ContactSectionRequest();
            ContactRequest contactRequest = new ContactRequest();
            contactRequest.setId("contact-id");
            request.setContacts(List.of(contactRequest));

            testSection.setContacts(new ArrayList<>(List.of(testContact, otherContact)));

            when(contactSectionRepository.findById("section-id")).thenReturn(Optional.of(testSection));
            when(contactRepository.findById("contact-id")).thenReturn(Optional.of(testContact));
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            contactService.updateSection("section-id", request, null);

            verify(fileService).deleteFile("old-photo.jpg");
            verify(contactRepository).deleteById("other-contact");
        }

        @Test
        @DisplayName("Should update contact with new photo")
        void updateSection_shouldUpdateContactWithPhoto() throws IOException {
            ContactSectionRequest request = new ContactSectionRequest();
            ContactRequest contactRequest = new ContactRequest();
            contactRequest.setId("contact-id");
            contactRequest.setPhotoIndex(0);
            request.setContacts(List.of(contactRequest));

            testContact.setPhotoPath("old-photo.jpg");
            testSection.setContacts(new ArrayList<>(List.of(testContact)));

            when(contactSectionRepository.findById("section-id")).thenReturn(Optional.of(testSection));
            when(contactRepository.findById("contact-id")).thenReturn(Optional.of(testContact));
            when(multipartFile.isEmpty()).thenReturn(false);
            when(fileService.deleteFile("old-photo.jpg")).thenReturn(true);
            when(fileService.uploadFile(multipartFile)).thenReturn("new-photo.jpg");
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            contactService.updateSection("section-id", request, List.of(multipartFile));

            verify(fileService).deleteFile("old-photo.jpg");
            verify(fileService).uploadFile(multipartFile);
        }

        @Test
        @DisplayName("Should create new contact when ID not found")
        void updateSection_shouldCreateNewContactWhenIdNotFound() throws IOException {
            ContactSectionRequest request = new ContactSectionRequest();
            ContactRequest contactRequest = new ContactRequest();
            contactRequest.setId("unknown-id");
            request.setContacts(List.of(contactRequest));

            testSection.setContacts(new ArrayList<>());

            when(contactSectionRepository.findById("section-id")).thenReturn(Optional.of(testSection));
            when(contactRepository.findById("unknown-id")).thenReturn(Optional.empty());
            when(contactMapper.toEntity(any(ContactRequest.class))).thenReturn(testContact);
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            ContactSectionResponse result = contactService.updateSection("section-id", request, null);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw when section not found")
        void updateSection_shouldThrowWhenNotFound() {
            ContactSectionRequest request = new ContactSectionRequest();
            when(contactSectionRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.updateSection("unknown", request, null))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle null contacts in request")
        void updateSection_shouldHandleNullContacts() throws IOException {
            ContactSectionRequest request = new ContactSectionRequest();
            request.setContacts(null);

            when(contactSectionRepository.findById("section-id")).thenReturn(Optional.of(testSection));
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            when(contactMapper.toSectionResponse(any(ContactSection.class))).thenReturn(testSectionResponse);

            ContactSectionResponse result = contactService.updateSection("section-id", request, null);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Delete Section")
    class DeleteSection {
        @Test
        @DisplayName("Should delete section with contacts")
        void deleteSection_shouldDeleteWithContacts() {
            testSection.setContacts(List.of(testContact));
            when(contactSectionRepository.findById("section-id")).thenReturn(Optional.of(testSection));
            doNothing().when(contactRepository).deleteById("contact-id");
            doNothing().when(contactSectionRepository).deleteById("section-id");

            contactService.deleteSection("section-id");

            verify(contactRepository).deleteById("contact-id");
            verify(contactSectionRepository).deleteById("section-id");
        }

        @Test
        @DisplayName("Should throw exception when section not found")
        void deleteSection_shouldThrowException() {
            when(contactSectionRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.deleteSection("unknown"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Delete Contact")
    class DeleteContact {
        @Test
        @DisplayName("Should delete contact")
        void deleteContact_shouldDelete() {
            when(contactRepository.findById("contact-id")).thenReturn(Optional.of(testContact));
            doNothing().when(contactRepository).deleteById("contact-id");

            contactService.deleteContact("contact-id");

            verify(contactRepository).deleteById("contact-id");
        }

        @Test
        @DisplayName("Should delete contact with photo")
        void deleteContact_withPhoto_shouldDeleteFile() throws IOException {
            testContact.setPhotoPath("photo.jpg");
            when(contactRepository.findById("contact-id")).thenReturn(Optional.of(testContact));
            when(fileService.deleteFile("photo.jpg")).thenReturn(true);
            doNothing().when(contactRepository).deleteById("contact-id");

            contactService.deleteContact("contact-id");

            verify(fileService).deleteFile("photo.jpg");
            verify(contactRepository).deleteById("contact-id");
        }
    }
}
