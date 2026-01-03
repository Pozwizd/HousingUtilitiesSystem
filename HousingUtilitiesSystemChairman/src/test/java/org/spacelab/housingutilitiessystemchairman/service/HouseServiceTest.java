package org.spacelab.housingutilitiessystemchairman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.entity.location.Street;
import org.spacelab.housingutilitiessystemchairman.exception.OperationException;
import org.spacelab.housingutilitiessystemchairman.mappers.HouseMapper;
import org.spacelab.housingutilitiessystemchairman.models.filters.house.HouseRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.location.HouseRequest;
import org.spacelab.housingutilitiessystemchairman.models.location.HouseResponse;
import org.spacelab.housingutilitiessystemchairman.models.location.HouseResponseTable;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.HouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HouseService Tests")
class HouseServiceTest {

    @Mock
    private HouseRepository houseRepository;

    @Mock
    private HouseMapper houseMapper;

    @Mock
    private StreetService streetService;

    @Mock
    private ChairmanService chairmanService;

    @InjectMocks
    private HouseService houseService;

    private House testHouse;
    private HouseRequest testHouseRequest;
    private HouseResponse testHouseResponse;
    private Street testStreet;
    private Chairman testChairman;

    @BeforeEach
    void setUp() {
        testHouse = new House();
        testHouse.setId("house-id");
        testHouse.setHouseNumber("123");

        testHouseRequest = new HouseRequest();
        testHouseRequest.setHouseNumber("123");
        testHouseRequest.setStatus("ACTIVE");

        testHouseResponse = new HouseResponse();
        testHouseResponse.setId("house-id");

        testStreet = new Street();
        testStreet.setId("street-id");

        testChairman = new Chairman();
        testChairman.setId("chairman-id");
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnHouse() {
            when(houseRepository.findById("house-id")).thenReturn(Optional.of(testHouse));
            Optional<House> result = houseService.findById("house-id");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should save house")
        void save_shouldSaveHouse() {
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            House result = houseService.save(testHouse);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find all houses")
        void findAll_shouldReturnAllHouses() {
            when(houseRepository.findAll()).thenReturn(Arrays.asList(testHouse, new House()));
            List<House> result = houseService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete by id")
        void deleteById_shouldDeleteHouse() {
            doNothing().when(houseRepository).deleteById("house-id");
            houseService.deleteById("house-id");
            verify(houseRepository).deleteById("house-id");
        }

        @Test
        @DisplayName("Should save all")
        void saveAll_shouldSaveAllHouses() {
            List<House> houses = Arrays.asList(testHouse, new House());
            when(houseRepository.saveAll(houses)).thenReturn(houses);
            List<House> result = houseService.saveAll(houses);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllHouses() {
            doNothing().when(houseRepository).deleteAll();
            houseService.deleteAll();
            verify(houseRepository).deleteAll();
        }
    }

    @Nested
    @DisplayName("Get House By Id")
    class GetHouseById {
        @Test
        @DisplayName("Should get house by id")
        void getHouseById_shouldReturnResponse() {
            when(houseRepository.findById("house-id")).thenReturn(Optional.of(testHouse));
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            HouseResponse result = houseService.getHouseById("house-id");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getHouseById_shouldThrowException() {
            when(houseRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> houseService.getHouseById("unknown"))
                    .isInstanceOf(OperationException.class);
        }
    }

    @Nested
    @DisplayName("Create House")
    class CreateHouse {
        @Test
        @DisplayName("Should create house successfully")
        void createHouse_shouldCreate() {
            when(houseMapper.toEntity(testHouseRequest)).thenReturn(testHouse);
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            HouseResponse result = houseService.createHouse(testHouseRequest);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should create house with street")
        void createHouse_withStreet_shouldSetStreet() {
            testHouseRequest.setStreetId("street-id");
            when(houseMapper.toEntity(testHouseRequest)).thenReturn(testHouse);
            when(streetService.findById("street-id")).thenReturn(Optional.of(testStreet));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.createHouse(testHouseRequest);

            verify(streetService).findById("street-id");
        }

        @Test
        @DisplayName("Should throw exception when street not found")
        void createHouse_shouldThrowException_whenStreetNotFound() {
            testHouseRequest.setStreetId("nonexistent");
            when(houseMapper.toEntity(testHouseRequest)).thenReturn(testHouse);
            when(streetService.findById("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> houseService.createHouse(testHouseRequest))
                    .isInstanceOf(OperationException.class);
        }

        @Test
        @DisplayName("Should create house with chairman")
        void createHouse_withChairman_shouldSetChairman() {
            testHouseRequest.setChairmanId("chairman-id");
            when(houseMapper.toEntity(testHouseRequest)).thenReturn(testHouse);
            when(chairmanService.findById("chairman-id")).thenReturn(Optional.of(testChairman));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.createHouse(testHouseRequest);

            verify(chairmanService).findById("chairman-id");
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void createHouse_shouldThrowException_forInvalidStatus() {
            testHouseRequest.setStatus("INVALID");
            when(houseMapper.toEntity(testHouseRequest)).thenReturn(testHouse);

            assertThatThrownBy(() -> houseService.createHouse(testHouseRequest))
                    .isInstanceOf(OperationException.class);
        }
    }

    @Nested
    @DisplayName("Update House")
    class UpdateHouse {
        @Test
        @DisplayName("Should update house successfully")
        void updateHouse_shouldUpdate() {
            when(houseRepository.findById("house-id")).thenReturn(Optional.of(testHouse));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            HouseResponse result = houseService.updateHouse("house-id", testHouseRequest);

            assertThat(result).isNotNull();
            verify(houseMapper).partialUpdate(testHouseRequest, testHouse);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void updateHouse_shouldThrowException_whenNotFound() {
            when(houseRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> houseService.updateHouse("unknown", testHouseRequest))
                    .isInstanceOf(OperationException.class);
        }

        @Test
        @DisplayName("Should remove chairman when empty string")
        void updateHouse_shouldRemoveChairman_whenEmptyString() {
            testHouseRequest.setChairmanId("");
            when(houseRepository.findById("house-id")).thenReturn(Optional.of(testHouse));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.updateHouse("house-id", testHouseRequest);

            assertThat(testHouse.getChairman()).isNull();
        }
    }

    @Nested
    @DisplayName("Delete House")
    class DeleteHouse {
        @Test
        @DisplayName("Should delete house successfully")
        void deleteHouse_shouldDelete() {
            when(houseRepository.existsById("house-id")).thenReturn(true);
            doNothing().when(houseRepository).deleteById("house-id");

            boolean result = houseService.deleteHouse("house-id");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void deleteHouse_shouldThrowException_whenNotFound() {
            when(houseRepository.existsById("unknown")).thenReturn(false);

            assertThatThrownBy(() -> houseService.deleteHouse("unknown"))
                    .isInstanceOf(OperationException.class);
        }
    }

    @Nested
    @DisplayName("Table and Search")
    class TableAndSearch {
        @Test
        @DisplayName("Should get houses table")
        void getHousesTable_shouldReturnTable() {
            HouseRequestTable requestTable = new HouseRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            Page<House> housesPage = new PageImpl<>(List.of(testHouse));
            Page<HouseResponseTable> responseTablePage = new PageImpl<>(List.of(new HouseResponseTable()));

            when(houseRepository.findHousesWithFilters(requestTable)).thenReturn(housesPage);
            when(houseMapper.toResponseTablePage(housesPage)).thenReturn(responseTablePage);

            Page<HouseResponseTable> result = houseService.getHousesTable(requestTable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find by street id")
        void findByStreetId_shouldReturnHouses() {
            when(houseRepository.findByStreetId("street-id")).thenReturn(List.of(testHouse));
            when(houseMapper.toResponseList(any())).thenReturn(List.of(testHouseResponse));

            List<HouseResponse> result = houseService.findByStreetId("street-id");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should search by street and number")
        void searchByStreetAndNumber_shouldSearch() {
            when(houseRepository.findByStreetIdAndHouseNumberContainingIgnoreCase("street-id", "12"))
                    .thenReturn(List.of(testHouse));
            when(houseMapper.toResponseList(any())).thenReturn(List.of(testHouseResponse));

            List<HouseResponse> result = houseService.searchByStreetAndNumber("street-id", "12");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return all when number is empty")
        void searchByStreetAndNumber_shouldReturnAll_whenNumberEmpty() {
            when(houseRepository.findByStreetId("street-id")).thenReturn(List.of(testHouse));
            when(houseMapper.toResponseList(any())).thenReturn(List.of(testHouseResponse));

            List<HouseResponse> result = houseService.searchByStreetAndNumber("street-id", "");

            assertThat(result).hasSize(1);
        }
    }
}
