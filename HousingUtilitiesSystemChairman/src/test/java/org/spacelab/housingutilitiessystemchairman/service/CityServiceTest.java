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
import org.spacelab.housingutilitiessystemchairman.entity.location.City;
import org.spacelab.housingutilitiessystemchairman.mappers.CityMapper;
import org.spacelab.housingutilitiessystemchairman.models.location.CityResponse;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.CityRepository;

import java.util.Arrays;
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
    private ObjectId testCityId;
    private CityResponse testCityResponse;

    @BeforeEach
    void setUp() {
        testCityId = new ObjectId();
        testCity = new City();
        testCity.setId(testCityId.toHexString());
        testCity.setName("Test City");

        testCityResponse = new CityResponse();
        testCityResponse.setId(testCityId.toHexString());
        testCityResponse.setName("Test City");
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        @Test
        @DisplayName("Should save city")
        void save_shouldSaveCity() {
            when(cityRepository.save(any(City.class))).thenReturn(testCity);
            City result = cityService.save(testCity);
            assertThat(result).isNotNull();
            verify(cityRepository).save(testCity);
        }

        @Test
        @DisplayName("Should save all cities with Iterable")
        void saveAll_iterable_shouldSaveAllCities() {
            List<City> cities = Arrays.asList(testCity, new City());
            when(cityRepository.saveAll(any(Iterable.class))).thenReturn(cities);
            List<City> result = cityService.saveAll((Iterable<City>) cities);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should save all cities with List")
        void saveAll_list_shouldSaveAllCities() {
            List<City> cities = Arrays.asList(testCity, new City());
            when(cityRepository.saveAll(cities)).thenReturn(cities);
            List<City> result = cityService.saveAll(cities);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnCity() {
            when(cityRepository.findById(testCityId)).thenReturn(Optional.of(testCity));
            Optional<City> result = cityService.findById(testCityId);
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when not found")
        void findById_shouldReturnEmpty() {
            when(cityRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());
            Optional<City> result = cityService.findById(new ObjectId());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all cities")
        void findAll_shouldReturnAllCities() {
            when(cityRepository.findAll()).thenReturn(Arrays.asList(testCity, new City()));
            List<City> result = cityService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete by id")
        void deleteById_shouldDeleteCity() {
            doNothing().when(cityRepository).deleteById(testCityId);
            cityService.deleteById(testCityId);
            verify(cityRepository).deleteById(testCityId);
        }

        @Test
        @DisplayName("Should update city")
        void update_shouldUpdateCity() {
            when(cityRepository.save(any(City.class))).thenReturn(testCity);
            City result = cityService.update(testCity);
            assertThat(result).isNotNull();
            verify(cityRepository).save(testCity);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllCities() {
            doNothing().when(cityRepository).deleteAll();
            cityService.deleteAll();
            verify(cityRepository).deleteAll();
        }
    }

    @Nested
    @DisplayName("Search Operations")
    class SearchOperations {
        @Test
        @DisplayName("Should search by name when name provided")
        void searchByName_shouldSearchWithName() {
            List<City> cities = List.of(testCity);
            List<CityResponse> responses = List.of(testCityResponse);

            when(cityRepository.findByNameContainingIgnoreCase("Test")).thenReturn(cities);
            when(cityMapper.toResponse(cities)).thenReturn(responses);

            List<CityResponse> result = cityService.searchByName("Test");

            assertThat(result).hasSize(1);
            verify(cityRepository).findByNameContainingIgnoreCase("Test");
        }

        @Test
        @DisplayName("Should return all cities when name is null")
        void searchByName_shouldReturnAll_whenNameNull() {
            List<City> cities = List.of(testCity);
            List<CityResponse> responses = List.of(testCityResponse);

            when(cityRepository.findAll()).thenReturn(cities);
            when(cityMapper.toResponse(cities)).thenReturn(responses);

            List<CityResponse> result = cityService.searchByName(null);

            assertThat(result).hasSize(1);
            verify(cityRepository).findAll();
        }

        @Test
        @DisplayName("Should return all cities when name is empty")
        void searchByName_shouldReturnAll_whenNameEmpty() {
            List<City> cities = List.of(testCity);
            List<CityResponse> responses = List.of(testCityResponse);

            when(cityRepository.findAll()).thenReturn(cities);
            when(cityMapper.toResponse(cities)).thenReturn(responses);

            List<CityResponse> result = cityService.searchByName("   ");

            assertThat(result).hasSize(1);
            verify(cityRepository).findAll();
        }
    }
}
