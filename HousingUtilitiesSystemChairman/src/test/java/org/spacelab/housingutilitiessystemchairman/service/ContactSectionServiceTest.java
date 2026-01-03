package org.spacelab.housingutilitiessystemchairman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.ContactSection;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ContactSectionRepository;

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

    private ContactSection testSection;

    @BeforeEach
    void setUp() {
        testSection = new ContactSection();
        testSection.setId("section-id");
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        @Test
        @DisplayName("Should save contact section")
        void save_shouldSaveSection() {
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            ContactSection result = contactSectionService.save(testSection);
            assertThat(result).isNotNull();
            verify(contactSectionRepository).save(testSection);
        }

        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnSection() {
            when(contactSectionRepository.findById("section-id")).thenReturn(Optional.of(testSection));
            Optional<ContactSection> result = contactSectionService.findById("section-id");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when not found")
        void findById_shouldReturnEmpty() {
            when(contactSectionRepository.findById("unknown")).thenReturn(Optional.empty());
            Optional<ContactSection> result = contactSectionService.findById("unknown");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all sections")
        void findAll_shouldReturnAllSections() {
            when(contactSectionRepository.findAll()).thenReturn(Arrays.asList(testSection, new ContactSection()));
            List<ContactSection> result = contactSectionService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete by id")
        void deleteById_shouldDeleteSection() {
            doNothing().when(contactSectionRepository).deleteById("section-id");
            contactSectionService.deleteById("section-id");
            verify(contactSectionRepository).deleteById("section-id");
        }

        @Test
        @DisplayName("Should update section")
        void update_shouldUpdateSection() {
            when(contactSectionRepository.save(any(ContactSection.class))).thenReturn(testSection);
            ContactSection result = contactSectionService.update(testSection);
            assertThat(result).isNotNull();
            verify(contactSectionRepository).save(testSection);
        }

        @Test
        @DisplayName("Should save all sections")
        void saveAll_shouldSaveAllSections() {
            List<ContactSection> sections = Arrays.asList(testSection, new ContactSection());
            when(contactSectionRepository.saveAll(sections)).thenReturn(sections);
            List<ContactSection> result = contactSectionService.saveAll(sections);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllSections() {
            doNothing().when(contactSectionRepository).deleteAll();
            contactSectionService.deleteAll();
            verify(contactSectionRepository).deleteAll();
        }
    }
}
