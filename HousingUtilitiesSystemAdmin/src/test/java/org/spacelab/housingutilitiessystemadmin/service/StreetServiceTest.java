package org.spacelab.housingutilitiessystemadmin.service;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.spacelab.housingutilitiessystemadmin.mappers.StreetMapper;
import org.spacelab.housingutilitiessystemadmin.models.location.StreetResponse;
import org.spacelab.housingutilitiessystemadmin.repository.StreetRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StreetService Tests")
class StreetServiceTest {

    @Mock
    private StreetRepository streetRepository;

    @Mock
    private StreetMapper streetMapper;

    @InjectMocks
    private StreetService streetService;

    private Street testStreet;
    private String testId;
    private ObjectId testObjectId;

    @BeforeEach
    void setUp() {
        testId = "street-id-123";
        testObjectId = new ObjectId();
        testStreet = new Street();
        testStreet.setId(testId);
        testStreet.setName("Тверская");
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save street")
        void save_shouldSaveStreet() {
            // Given
            when(streetRepository.save(testStreet)).thenReturn(testStreet);

            // When
            Street result = streetService.save(testStreet);

            // Then
            assertThat(result).isEqualTo(testStreet);
            verify(streetRepository).save(testStreet);
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find street by String id")
        void findById_string_shouldReturnStreet() {
            // Given
            String streetId = testId;
            when(streetRepository.findById(streetId)).thenReturn(Optional.of(testStreet));

            // When
            Optional<Street> result = streetService.findById(streetId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Тверская");
        }

        @Test
        @DisplayName("Should find street by ObjectId")
        void findById_objectId_shouldReturnStreet() {
            // Given
            when(streetRepository.findById(testObjectId.toString())).thenReturn(Optional.of(testStreet));

            // When
            Optional<Street> result = streetService.findById(testObjectId);

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when street not found")
        void findById_shouldReturnEmpty_whenNotFound() {
            // Given
            String strId = testId;
            when(streetRepository.findById(strId)).thenReturn(Optional.empty());

            // When
            Optional<Street> result = streetService.findById(strId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all streets")
        void findAll_shouldReturnAllStreets() {
            // Given
            List<Street> streets = Arrays.asList(testStreet, new Street());
            when(streetRepository.findAll()).thenReturn(streets);

            // When
            List<Street> result = streetService.findAll();

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByCityId Tests")
    class FindByCityIdTests {

        @Test
        @DisplayName("Should find streets by city id")
        void findByCityId_shouldReturnStreets() {
            // Given
            String cityId = "city-id-123";
            List<Street> streets = Arrays.asList(testStreet);
            when(streetRepository.findByCityId(cityId)).thenReturn(streets);

            // When
            List<Street> result = streetService.findByCityId(cityId);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("searchByCityAndName Tests")
    class SearchByCityAndNameTests {

        @Test
        @DisplayName("Should return all streets for city when name is null")
        void searchByCityAndName_shouldReturnAll_whenNameIsNull() {
            // Given
            String cityId = "city-id-123";
            List<Street> streets = Arrays.asList(testStreet);
            List<StreetResponse> responses = Arrays.asList(new StreetResponse());
            when(streetRepository.findByCityId(cityId)).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            // When
            List<StreetResponse> result = streetService.searchByCityAndName(cityId, null);

            // Then
            assertThat(result).hasSize(1);
            verify(streetRepository).findByCityId(cityId);
        }

        @Test
        @DisplayName("Should return all streets for city when name is empty")
        void searchByCityAndName_shouldReturnAll_whenNameIsEmpty() {
            // Given
            String cityId = "city-id-123";
            List<Street> streets = Arrays.asList(testStreet);
            List<StreetResponse> responses = Arrays.asList(new StreetResponse());
            when(streetRepository.findByCityId(cityId)).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            // When
            List<StreetResponse> result = streetService.searchByCityAndName(cityId, "   ");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should search streets by city and name")
        void searchByCityAndName_shouldSearch() {
            // Given
            String cityId = "city-id-123";
            List<Street> streets = Arrays.asList(testStreet);
            List<StreetResponse> responses = Arrays.asList(new StreetResponse());
            when(streetRepository.findByCityIdAndNameContainingIgnoreCase(cityId, "Твер")).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            // When
            List<StreetResponse> result = streetService.searchByCityAndName(cityId, "  Твер  ");

            // Then
            assertThat(result).hasSize(1);
            verify(streetRepository).findByCityIdAndNameContainingIgnoreCase(cityId, "Твер");
        }
    }

    @Nested
    @DisplayName("searchByName Tests")
    class SearchByNameTests {

        @Test
        @DisplayName("Should return all streets when name is null")
        void searchByName_shouldReturnAll_whenNameIsNull() {
            // Given
            List<Street> streets = Arrays.asList(testStreet);
            List<StreetResponse> responses = Arrays.asList(new StreetResponse());
            when(streetRepository.findAll()).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            // When
            List<StreetResponse> result = streetService.searchByName(null);

            // Then
            assertThat(result).hasSize(1);
            verify(streetRepository).findAll();
        }

        @Test
        @DisplayName("Should return all streets when name is empty")
        void searchByName_shouldReturnAll_whenNameIsEmpty() {
            // Given
            List<Street> streets = Arrays.asList(testStreet);
            List<StreetResponse> responses = Arrays.asList(new StreetResponse());
            when(streetRepository.findAll()).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            // When
            List<StreetResponse> result = streetService.searchByName("   ");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should search streets by name")
        void searchByName_shouldSearch() {
            // Given
            List<Street> streets = Arrays.asList(testStreet);
            List<StreetResponse> responses = Arrays.asList(new StreetResponse());
            when(streetRepository.findByNameContainingIgnoreCase("Твер")).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            // When
            List<StreetResponse> result = streetService.searchByName("  Твер  ");

            // Then
            verify(streetRepository).findByNameContainingIgnoreCase("Твер");
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should delete street by String id")
        void deleteById_string_shouldDelete() {
            // When
            streetService.deleteById(testId);

            // Then
            verify(streetRepository).deleteById(testId);
        }

        @Test
        @DisplayName("Should delete street by ObjectId")
        void deleteById_objectId_shouldDelete() {
            // When
            streetService.deleteById(testObjectId);

            // Then
            verify(streetRepository).deleteById(testObjectId.toString());
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update street")
        void update_shouldUpdateStreet() {
            // Given
            when(streetRepository.save(testStreet)).thenReturn(testStreet);

            // When
            Street result = streetService.update(testStreet);

            // Then
            assertThat(result).isEqualTo(testStreet);
        }
    }

    @Nested
    @DisplayName("saveAll Tests")
    class SaveAllTests {

        @Test
        @DisplayName("Should save all streets")
        void saveAll_shouldSaveAllStreets() {
            // Given
            List<Street> streets = Arrays.asList(testStreet, new Street());
            when(streetRepository.saveAll(streets)).thenReturn(streets);

            // When
            List<Street> result = streetService.saveAll(streets);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("deleteAll Tests")
    class DeleteAllTests {

        @Test
        @DisplayName("Should delete all streets")
        void deleteAll_shouldDeleteAllStreets() {
            // When
            streetService.deleteAll();

            // Then
            verify(streetRepository).deleteAll();
        }
    }
}
