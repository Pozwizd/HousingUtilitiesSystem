package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.Contact;
import org.spacelab.housingutilitiessystemadmin.repository.ContactRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Tests")
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    private Contact testContact;

    @BeforeEach
    void setUp() {
        testContact = new Contact();
        testContact.setId("contact-id-123");
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {
        @Test
        @DisplayName("Should save and return contact")
        void save_shouldSaveAndReturnContact() {
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

            Contact result = contactService.save(testContact);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("contact-id-123");
            verify(contactRepository).save(testContact);
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {
        @Test
        @DisplayName("Should return Optional with contact when found")
        void findById_shouldReturnOptionalWithContact() {
            when(contactRepository.findById("contact-id-123")).thenReturn(Optional.of(testContact));

            Optional<Contact> result = contactService.findById("contact-id-123");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("contact-id-123");
        }

        @Test
        @DisplayName("Should return empty Optional when not found")
        void findById_shouldReturnEmptyOptional() {
            when(contactRepository.findById("nonexistent")).thenReturn(Optional.empty());

            Optional<Contact> result = contactService.findById("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {
        @Test
        @DisplayName("Should return all contacts")
        void findAll_shouldReturnAllContacts() {
            Contact contact2 = new Contact();
            contact2.setId("contact-id-456");
            List<Contact> contacts = Arrays.asList(testContact, contact2);
            when(contactRepository.findAll()).thenReturn(contacts);

            List<Contact> result = contactService.findAll();

            assertThat(result).hasSize(2);
            verify(contactRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no contacts")
        void findAll_shouldReturnEmptyList() {
            when(contactRepository.findAll()).thenReturn(List.of());

            List<Contact> result = contactService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {
        @Test
        @DisplayName("Should delete contact by id")
        void deleteById_shouldDeleteContact() {
            doNothing().when(contactRepository).deleteById("contact-id-123");

            contactService.deleteById("contact-id-123");

            verify(contactRepository).deleteById("contact-id-123");
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update and return contact")
        void update_shouldUpdateAndReturnContact() {
            when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

            Contact result = contactService.update(testContact);

            assertThat(result).isNotNull();
            verify(contactRepository).save(testContact);
        }
    }

    @Nested
    @DisplayName("saveAll Tests")
    class SaveAllTests {
        @Test
        @DisplayName("Should save all contacts")
        void saveAll_shouldSaveAllContacts() {
            Contact contact2 = new Contact();
            contact2.setId("contact-id-456");
            List<Contact> contacts = Arrays.asList(testContact, contact2);
            when(contactRepository.saveAll(contacts)).thenReturn(contacts);

            List<Contact> result = contactService.saveAll(contacts);

            assertThat(result).hasSize(2);
            verify(contactRepository).saveAll(contacts);
        }
    }

    @Nested
    @DisplayName("deleteAll Tests")
    class DeleteAllTests {
        @Test
        @DisplayName("Should delete all contacts")
        void deleteAll_shouldDeleteAllContacts() {
            doNothing().when(contactRepository).deleteAll();

            contactService.deleteAll();

            verify(contactRepository).deleteAll();
        }
    }
}
