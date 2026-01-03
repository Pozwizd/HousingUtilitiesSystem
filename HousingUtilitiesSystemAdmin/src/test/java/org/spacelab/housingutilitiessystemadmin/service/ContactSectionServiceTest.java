package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.ContactSection;
import org.spacelab.housingutilitiessystemadmin.repository.ContactSectionRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactSectionService Tests")
class ContactSectionServiceTest {

    @Mock
    private ContactSectionRepository contactSectionRepository;

    @InjectMocks
    private ContactSectionService contactSectionService;

    private ContactSection testContactSection;

    @BeforeEach
    void setUp() {
        testContactSection = new ContactSection();
        testContactSection.setId("section-id-123");
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {
        @Test
        @DisplayName("Should save and return contact section")
        void save_shouldSaveAndReturnContactSection() {
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testContactSection);

            ContactSection result = contactSectionService.save(testContactSection);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("section-id-123");
            verify(contactSectionRepository).save(testContactSection);
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {
        @Test
        @DisplayName("Should return Optional with contact section when found")
        void findById_shouldReturnOptionalWithContactSection() {
            when(contactSectionRepository.findById("section-id-123")).thenReturn(Optional.of(testContactSection));

            Optional<ContactSection> result = contactSectionService.findById("section-id-123");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("section-id-123");
        }

        @Test
        @DisplayName("Should return empty Optional when not found")
        void findById_shouldReturnEmptyOptional() {
            when(contactSectionRepository.findById("nonexistent")).thenReturn(Optional.empty());

            Optional<ContactSection> result = contactSectionService.findById("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {
        @Test
        @DisplayName("Should return all contact sections")
        void findAll_shouldReturnAllContactSections() {
            ContactSection section2 = new ContactSection();
            section2.setId("section-id-456");
            List<ContactSection> sections = Arrays.asList(testContactSection, section2);
            when(contactSectionRepository.findAll()).thenReturn(sections);

            List<ContactSection> result = contactSectionService.findAll();

            assertThat(result).hasSize(2);
            verify(contactSectionRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no contact sections")
        void findAll_shouldReturnEmptyList() {
            when(contactSectionRepository.findAll()).thenReturn(List.of());

            List<ContactSection> result = contactSectionService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {
        @Test
        @DisplayName("Should delete contact section by id")
        void deleteById_shouldDeleteContactSection() {
            doNothing().when(contactSectionRepository).deleteById("section-id-123");

            contactSectionService.deleteById("section-id-123");

            verify(contactSectionRepository).deleteById("section-id-123");
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update and return contact section")
        void update_shouldUpdateAndReturnContactSection() {
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testContactSection);

            ContactSection result = contactSectionService.update(testContactSection);

            assertThat(result).isNotNull();
            verify(contactSectionRepository).save(testContactSection);
        }
    }

    @Nested
    @DisplayName("saveAll Tests")
    class SaveAllTests {
        @Test
        @DisplayName("Should save all contact sections")
        void saveAll_shouldSaveAllContactSections() {
            ContactSection section2 = new ContactSection();
            section2.setId("section-id-456");
            List<ContactSection> sections = Arrays.asList(testContactSection, section2);
            when(contactSectionRepository.saveAll(sections)).thenReturn(sections);

            List<ContactSection> result = contactSectionService.saveAll(sections);

            assertThat(result).hasSize(2);
            verify(contactSectionRepository).saveAll(sections);
        }
    }

    @Nested
    @DisplayName("deleteAll Tests")
    class DeleteAllTests {
        @Test
        @DisplayName("Should delete all contact sections")
        void deleteAll_shouldDeleteAllContactSections() {
            doNothing().when(contactSectionRepository).deleteAll();

            contactSectionService.deleteAll();

            verify(contactSectionRepository).deleteAll();
        }
    }
}
