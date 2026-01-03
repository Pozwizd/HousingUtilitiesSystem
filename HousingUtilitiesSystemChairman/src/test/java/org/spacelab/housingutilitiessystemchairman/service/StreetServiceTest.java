package org.spacelab.housingutilitiessystemchairman.service;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.location.Street;
import org.spacelab.housingutilitiessystemchairman.mappers.StreetMapper;
import org.spacelab.housingutilitiessystemchairman.models.location.StreetResponse;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.StreetRepository;

import java.util.Arrays;
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
    private StreetResponse testStreetResponse;

    @BeforeEach
    void setUp() {
        testStreet = new Street();
        testStreet.setId("street-id");
        testStreet.setName("Test Street");

        testStreetResponse = new StreetResponse();
        testStreetResponse.setId("street-id");
        testStreetResponse.setName("Test Street");
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        @Test
        @DisplayName("Should save street")
        void save_shouldSaveStreet() {
            when(streetRepository.save(any(Street.class))).thenReturn(testStreet);
            Street result = streetService.save(testStreet);
            assertThat(result).isNotNull();
            verify(streetRepository).save(testStreet);
        }

        @Test
        @DisplayName("Should find by id with String")
        void findById_string_shouldReturnStreet() {
            when(streetRepository.findById("street-id")).thenReturn(Optional.of(testStreet));
            Optional<Street> result = streetService.findById("street-id");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should find by id with ObjectId")
        void findById_objectId_shouldReturnStreet() {
            ObjectId objectId = new ObjectId();
            when(streetRepository.findById(objectId.toString())).thenReturn(Optional.of(testStreet));
            Optional<Street> result = streetService.findById(objectId);
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when not found")
        void findById_shouldReturnEmpty() {
            when(streetRepository.findById("unknown")).thenReturn(Optional.empty());
            Optional<Street> result = streetService.findById("unknown");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all streets")
        void findAll_shouldReturnAllStreets() {
            when(streetRepository.findAll()).thenReturn(Arrays.asList(testStreet, new Street()));
            List<Street> result = streetService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete by id with String")
        void deleteById_string_shouldDeleteStreet() {
            doNothing().when(streetRepository).deleteById("street-id");
            streetService.deleteById("street-id");
            verify(streetRepository).deleteById("street-id");
        }

        @Test
        @DisplayName("Should delete by id with ObjectId")
        void deleteById_objectId_shouldDeleteStreet() {
            ObjectId objectId = new ObjectId();
            doNothing().when(streetRepository).deleteById(objectId.toString());
            streetService.deleteById(objectId);
            verify(streetRepository).deleteById(objectId.toString());
        }

        @Test
        @DisplayName("Should update street")
        void update_shouldUpdateStreet() {
            when(streetRepository.save(any(Street.class))).thenReturn(testStreet);
            Street result = streetService.update(testStreet);
            assertThat(result).isNotNull();
            verify(streetRepository).save(testStreet);
        }

        @Test
        @DisplayName("Should save all streets")
        void saveAll_shouldSaveAllStreets() {
            List<Street> streets = Arrays.asList(testStreet, new Street());
            when(streetRepository.saveAll(streets)).thenReturn(streets);
            List<Street> result = streetService.saveAll(streets);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllStreets() {
            doNothing().when(streetRepository).deleteAll();
            streetService.deleteAll();
            verify(streetRepository).deleteAll();
        }

        @Test
        @DisplayName("Should find by city id")
        void findByCityId_shouldReturnStreets() {
            when(streetRepository.findByCityId("city-id")).thenReturn(List.of(testStreet));
            List<Street> result = streetService.findByCityId("city-id");
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Search Operations")
    class SearchOperations {
        @Test
        @DisplayName("Should search by city and name")
        void searchByCityAndName_shouldSearch() {
            List<Street> streets = List.of(testStreet);
            List<StreetResponse> responses = List.of(testStreetResponse);

            when(streetRepository.findByCityIdAndNameContainingIgnoreCase("city-id", "Test")).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            List<StreetResponse> result = streetService.searchByCityAndName("city-id", "Test");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return all streets for city when name is null")
        void searchByCityAndName_shouldReturnAll_whenNameNull() {
            List<Street> streets = List.of(testStreet);
            List<StreetResponse> responses = List.of(testStreetResponse);

            when(streetRepository.findByCityId("city-id")).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            List<StreetResponse> result = streetService.searchByCityAndName("city-id", null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should search by name when provided")
        void searchByName_shouldSearch() {
            List<Street> streets = List.of(testStreet);
            List<StreetResponse> responses = List.of(testStreetResponse);

            when(streetRepository.findByNameContainingIgnoreCase("Test")).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            List<StreetResponse> result = streetService.searchByName("Test");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return all streets when name is null")
        void searchByName_shouldReturnAll_whenNameNull() {
            List<Street> streets = List.of(testStreet);
            List<StreetResponse> responses = List.of(testStreetResponse);

            when(streetRepository.findAll()).thenReturn(streets);
            when(streetMapper.toResponse(streets)).thenReturn(responses);

            List<StreetResponse> result = streetService.searchByName(null);

            assertThat(result).hasSize(1);
        }
    }
}
