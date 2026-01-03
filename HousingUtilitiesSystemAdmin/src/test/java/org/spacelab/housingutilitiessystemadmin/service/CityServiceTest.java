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
import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.spacelab.housingutilitiessystemadmin.mappers.CityMapper;
import org.spacelab.housingutilitiessystemadmin.models.location.CityResponse;
import org.spacelab.housingutilitiessystemadmin.repository.CityRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CityService Tests")
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityMapper cityMapper;

    @InjectMocks
    private CityService cityService;

    private City testCity;
    private ObjectId testObjectId;

    @BeforeEach
    void setUp() {
        testObjectId = new ObjectId();
        testCity = new City();
        testCity.setId(testObjectId.toString()); // City.id is String
        testCity.setName("Москва");
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save city")
        void save_shouldSaveCity() {
            when(cityRepository.save(testCity)).thenReturn(testCity);

            City result = cityService.save(testCity);

            assertThat(result).isEqualTo(testCity);
            verify(cityRepository).save(testCity);
        }
    }

    @Nested
    @DisplayName("saveAll Tests")
    class SaveAllTests {

        @Test
        @DisplayName("Should save all cities from Iterable")
        void saveAll_iterable_shouldSaveAllCities() {
            List<City> cities = Arrays.asList(testCity, new City());
            when(cityRepository.saveAll(cities)).thenReturn(cities);

            List<City> result = cityService.saveAll((Iterable<City>) cities);

            assertThat(result).hasSize(2);
            verify(cityRepository).saveAll(cities);
        }

        @Test
        @DisplayName("Should save all cities from List")
        void saveAll_list_shouldSaveAllCities() {
            List<City> cities = Arrays.asList(testCity, new City());
            when(cityRepository.saveAll(cities)).thenReturn(cities);

            List<City> result = cityService.saveAll(cities);

            assertThat(result).hasSize(2);
            verify(cityRepository).saveAll(cities);
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find city by id")
        void findById_shouldReturnCity() {
            // CityService.findById takes ObjectId and passes it to repository
            when(cityRepository.findById(testObjectId)).thenReturn(Optional.of(testCity));

            Optional<City> result = cityService.findById(testObjectId);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Москва");
        }

        @Test
        @DisplayName("Should return empty when city not found")
        void findById_shouldReturnEmpty_whenNotFound() {
            when(cityRepository.findById(testObjectId)).thenReturn(Optional.empty());

            Optional<City> result = cityService.findById(testObjectId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all cities")
        void findAll_shouldReturnAllCities() {
            List<City> cities = Arrays.asList(testCity, new City());
            when(cityRepository.findAll()).thenReturn(cities);

            List<City> result = cityService.findAll();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no cities")
        void findAll_shouldReturnEmptyList_whenNoCities() {
            when(cityRepository.findAll()).thenReturn(Collections.emptyList());

            List<City> result = cityService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchByName Tests")
    class SearchByNameTests {

        @Test
        @DisplayName("Should return all cities when name is null")
        void searchByName_shouldReturnAll_whenNameIsNull() {
            List<City> cities = Arrays.asList(testCity);
            List<CityResponse> responses = Arrays.asList(new CityResponse());
            when(cityRepository.findAll()).thenReturn(cities);
            when(cityMapper.toResponse(cities)).thenReturn(responses);

            List<CityResponse> result = cityService.searchByName(null);

            assertThat(result).hasSize(1);
            verify(cityRepository).findAll();
            verify(cityRepository, never()).findByNameContainingIgnoreCase(any());
        }

        @Test
        @DisplayName("Should return all cities when name is empty")
        void searchByName_shouldReturnAll_whenNameIsEmpty() {
            List<City> cities = Arrays.asList(testCity);
            List<CityResponse> responses = Arrays.asList(new CityResponse());
            when(cityRepository.findAll()).thenReturn(cities);
            when(cityMapper.toResponse(cities)).thenReturn(responses);

            List<CityResponse> result = cityService.searchByName("   ");

            assertThat(result).hasSize(1);
            verify(cityRepository).findAll();
        }

        @Test
        @DisplayName("Should search cities by name containing")
        void searchByName_shouldSearchByCities() {
            List<City> cities = Arrays.asList(testCity);
            List<CityResponse> responses = Arrays.asList(new CityResponse());
            when(cityRepository.findByNameContainingIgnoreCase("Моск")).thenReturn(cities);
            when(cityMapper.toResponse(cities)).thenReturn(responses);

            List<CityResponse> result = cityService.searchByName("Моск");

            assertThat(result).hasSize(1);
            verify(cityRepository).findByNameContainingIgnoreCase("Моск");
        }

        @Test
        @DisplayName("Should trim name before searching")
        void searchByName_shouldTrimName() {
            List<City> cities = Arrays.asList(testCity);
            List<CityResponse> responses = Arrays.asList(new CityResponse());
            when(cityRepository.findByNameContainingIgnoreCase("Моск")).thenReturn(cities);
            when(cityMapper.toResponse(cities)).thenReturn(responses);

            List<CityResponse> result = cityService.searchByName("  Моск  ");

            verify(cityRepository).findByNameContainingIgnoreCase("Моск");
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should delete city by id")
        void deleteById_shouldDeleteCity() {
            cityService.deleteById(testObjectId);

            verify(cityRepository).deleteById(testObjectId);
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update city")
        void update_shouldUpdateCity() {
            when(cityRepository.save(testCity)).thenReturn(testCity);

            City result = cityService.update(testCity);

            assertThat(result).isEqualTo(testCity);
            verify(cityRepository).save(testCity);
        }
    }

    @Nested
    @DisplayName("deleteAll Tests")
    class DeleteAllTests {

        @Test
        @DisplayName("Should delete all cities")
        void deleteAll_shouldDeleteAllCities() {
            cityService.deleteAll();

            verify(cityRepository).deleteAll();
        }
    }
}
