package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.Chairman;
import org.spacelab.housingutilitiessystemadmin.entity.location.House;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.spacelab.housingutilitiessystemadmin.exception.OperationException;
import org.spacelab.housingutilitiessystemadmin.mappers.HouseMapper;
import org.spacelab.housingutilitiessystemadmin.models.filters.house.HouseRequestTable;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseRequest;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseResponse;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseResponseTable;
import org.spacelab.housingutilitiessystemadmin.repository.HouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.Collections;
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
    private Street testStreet;
    private Chairman testChairman;
    private HouseResponse testHouseResponse;
    private String testId;

    @BeforeEach
    void setUp() {
        testId = "house-id-123";
        testHouse = new House();
        testHouse.setId(testId);
        testHouse.setHouseNumber("42");
        testHouse.setStatus(Status.ACTIVE);

        testStreet = new Street();
        testStreet.setId("street-id-123");
        testStreet.setName("Тверская");

        testChairman = new Chairman();
        testChairman.setId("chairman-id-123");
        testChairman.setFirstName("Иван");
        testChairman.setLastName("Иванов");

        testHouseResponse = new HouseResponse();
        testHouseResponse.setId(testId);
        testHouseResponse.setHouseNumber("42");
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperationsTests {

        @Test
        @DisplayName("findById should return house")
        void findById_shouldReturnHouse() {
            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));

            Optional<House> result = houseService.findById(testId);

            assertThat(result).isPresent();
            assertThat(result.get().getHouseNumber()).isEqualTo("42");
        }

        @Test
        @DisplayName("findById should return empty when not found")
        void findById_shouldReturnEmpty_whenNotFound() {
            when(houseRepository.findById(testId)).thenReturn(Optional.empty());

            Optional<House> result = houseService.findById(testId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("save should save house")
        void save_shouldSaveHouse() {
            when(houseRepository.save(testHouse)).thenReturn(testHouse);

            House result = houseService.save(testHouse);

            assertThat(result).isEqualTo(testHouse);
        }

        @Test
        @DisplayName("findAll should return all houses")
        void findAll_shouldReturnAllHouses() {
            List<House> houses = Arrays.asList(testHouse, new House());
            when(houseRepository.findAll()).thenReturn(houses);

            List<House> result = houseService.findAll();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("deleteById should delete house")
        void deleteById_shouldDeleteHouse() {
            houseService.deleteById(testId);

            verify(houseRepository).deleteById(testId);
        }

        @Test
        @DisplayName("saveAll should save all houses")
        void saveAll_shouldSaveAllHouses() {
            List<House> houses = Arrays.asList(testHouse, new House());
            when(houseRepository.saveAll(houses)).thenReturn(houses);

            List<House> result = houseService.saveAll(houses);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("deleteAll should delete all houses")
        void deleteAll_shouldDeleteAllHouses() {
            houseService.deleteAll();

            verify(houseRepository).deleteAll();
        }
    }

    @Nested
    @DisplayName("getHouseById Tests")
    class GetHouseByIdTests {

        @Test
        @DisplayName("Should return house response when found")
        void getHouseById_shouldReturnHouseResponse() {
            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            HouseResponse result = houseService.getHouseById(testId);

            assertThat(result.getId()).isEqualTo(testId);
        }

        @Test
        @DisplayName("Should throw exception when house not found")
        void getHouseById_shouldThrowException_whenNotFound() {
            when(houseRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> houseService.getHouseById(testId))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }
    }

    @Nested
    @DisplayName("createHouse Tests")
    class CreateHouseTests {

        @Test
        @DisplayName("Should create house with street and chairman")
        void createHouse_shouldCreateWithStreetAndChairman() {
            HouseRequest request = new HouseRequest();
            request.setHouseNumber("42");
            request.setStreetId("street-id-123");
            request.setChairmanId("chairman-id-123");
            request.setStatus("ACTIVE");

            when(houseMapper.toEntity(request)).thenReturn(testHouse);
            String streetId = "street-id-123";
            when(streetService.findById(streetId)).thenReturn(Optional.of(testStreet));
            when(chairmanService.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            HouseResponse result = houseService.createHouse(request);

            assertThat(result).isNotNull();
            verify(houseRepository).save(any(House.class));
        }

        @Test
        @DisplayName("Should create house without street when streetId is null")
        void createHouse_shouldCreateWithoutStreet_whenStreetIdIsNull() {
            HouseRequest request = new HouseRequest();
            request.setHouseNumber("42");
            request.setStreetId(null);

            when(houseMapper.toEntity(request)).thenReturn(testHouse);
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            HouseResponse result = houseService.createHouse(request);

            assertThat(result).isNotNull();
            verify(streetService, never()).findById(anyString());
        }

        @Test
        @DisplayName("Should throw exception when street not found")
        void createHouse_shouldThrowException_whenStreetNotFound() {
            HouseRequest request = new HouseRequest();
            request.setStreetId("nonexistent-street-id");

            when(houseMapper.toEntity(request)).thenReturn(testHouse);
            String nonExistentStreetId = "nonexistent-street-id";
            when(streetService.findById(nonExistentStreetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> houseService.createHouse(request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Улица");
        }

        @Test
        @DisplayName("Should not set chairman when chairmanId is null")
        void createHouse_shouldNotSetChairman_whenChairmanIdIsNull() {
            HouseRequest request = new HouseRequest();
            request.setChairmanId(null);

            when(houseMapper.toEntity(request)).thenReturn(testHouse);
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.createHouse(request);

            verify(chairmanService, never()).findById(any());
        }

        @Test
        @DisplayName("Should not set chairman when chairmanId is empty")
        void createHouse_shouldNotSetChairman_whenChairmanIdIsEmpty() {
            HouseRequest request = new HouseRequest();
            request.setChairmanId("");

            when(houseMapper.toEntity(request)).thenReturn(testHouse);
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.createHouse(request);

            verify(chairmanService, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw exception when chairman not found")
        void createHouse_shouldThrowException_whenChairmanNotFound() {
            HouseRequest request = new HouseRequest();
            request.setChairmanId("nonexistent-chairman-id");

            when(houseMapper.toEntity(request)).thenReturn(testHouse);
            when(chairmanService.findById("nonexistent-chairman-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> houseService.createHouse(request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Председатель");
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void createHouse_shouldThrowException_forInvalidStatus() {
            HouseRequest request = new HouseRequest();
            request.setStatus("INVALID_STATUS");

            when(houseMapper.toEntity(request)).thenReturn(testHouse);

            assertThatThrownBy(() -> houseService.createHouse(request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Неверный статус");
        }

        @Test
        @DisplayName("Should not set status when status is null")
        void createHouse_shouldNotSetStatus_whenStatusIsNull() {
            HouseRequest request = new HouseRequest();
            request.setStatus(null);

            when(houseMapper.toEntity(request)).thenReturn(testHouse);
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.createHouse(request);

            // No exception should be thrown
            verify(houseRepository).save(any(House.class));
        }
    }

    @Nested
    @DisplayName("updateHouse Tests")
    class UpdateHouseTests {

        @Test
        @DisplayName("Should update house successfully")
        void updateHouse_shouldUpdate() {
            HouseRequest request = new HouseRequest();
            request.setHouseNumber("43");

            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            HouseResponse result = houseService.updateHouse(testId, request);

            assertThat(result).isNotNull();
            verify(houseMapper).partialUpdate(request, testHouse);
        }

        @Test
        @DisplayName("Should throw exception when house not found")
        void updateHouse_shouldThrowException_whenNotFound() {
            when(houseRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> houseService.updateHouse(testId, new HouseRequest()))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }

        @Test
        @DisplayName("Should update street when streetId provided")
        void updateHouse_shouldUpdateStreet() {
            HouseRequest request = new HouseRequest();
            request.setStreetId("street-id-123");

            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));
            String streetId = "street-id-123";
            when(streetService.findById(streetId)).thenReturn(Optional.of(testStreet));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.updateHouse(testId, request);

            assertThat(testHouse.getStreet()).isEqualTo(testStreet);
        }

        @Test
        @DisplayName("Should throw exception when street not found on update")
        void updateHouse_shouldThrowException_whenStreetNotFound() {
            HouseRequest request = new HouseRequest();
            request.setStreetId("nonexistent-street-id");

            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));
            String nonExistentStreetId = "nonexistent-street-id";
            when(streetService.findById(nonExistentStreetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> houseService.updateHouse(testId, request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Улица");
        }

        @Test
        @DisplayName("Should remove chairman when chairmanId is empty")
        void updateHouse_shouldRemoveChairman_whenChairmanIdIsEmpty() {
            HouseRequest request = new HouseRequest();
            request.setChairmanId("");

            testHouse.setChairman(testChairman);
            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.updateHouse(testId, request);

            assertThat(testHouse.getChairman()).isNull();
        }

        @Test
        @DisplayName("Should update chairman when chairmanId provided")
        void updateHouse_shouldUpdateChairman() {
            HouseRequest request = new HouseRequest();
            request.setChairmanId("chairman-id-123");

            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));
            when(chairmanService.findById("chairman-id-123")).thenReturn(Optional.of(testChairman));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.updateHouse(testId, request);

            assertThat(testHouse.getChairman()).isEqualTo(testChairman);
        }

        @Test
        @DisplayName("Should throw exception when chairman not found on update")
        void updateHouse_shouldThrowException_whenChairmanNotFound() {
            HouseRequest request = new HouseRequest();
            request.setChairmanId("nonexistent-chairman-id");

            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));
            when(chairmanService.findById("nonexistent-chairman-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> houseService.updateHouse(testId, request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Председатель");
        }

        @Test
        @DisplayName("Should throw exception for invalid status on update")
        void updateHouse_shouldThrowException_forInvalidStatus() {
            HouseRequest request = new HouseRequest();
            request.setStatus("INVALID_STATUS");

            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));

            assertThatThrownBy(() -> houseService.updateHouse(testId, request))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Неверный статус");
        }

        @Test
        @DisplayName("Should update status when valid status provided")
        void updateHouse_shouldUpdateStatus() {
            HouseRequest request = new HouseRequest();
            request.setStatus("DEACTIVATED");

            when(houseRepository.findById(testId)).thenReturn(Optional.of(testHouse));
            when(houseRepository.save(any(House.class))).thenReturn(testHouse);
            when(houseMapper.mapHouseToResponse(testHouse)).thenReturn(testHouseResponse);

            houseService.updateHouse(testId, request);

            assertThat(testHouse.getStatus()).isEqualTo(Status.DEACTIVATED);
        }
    }

    @Nested
    @DisplayName("deleteHouse Tests")
    class DeleteHouseTests {

        @Test
        @DisplayName("Should delete house and return true")
        void deleteHouse_shouldDeleteAndReturnTrue() {
            when(houseRepository.existsById(testId)).thenReturn(true);

            boolean result = houseService.deleteHouse(testId);

            assertThat(result).isTrue();
            verify(houseRepository).deleteById(testId);
        }

        @Test
        @DisplayName("Should throw exception when house not found")
        void deleteHouse_shouldThrowException_whenNotFound() {
            when(houseRepository.existsById(testId)).thenReturn(false);

            assertThatThrownBy(() -> houseService.deleteHouse(testId))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }
    }

    @Nested
    @DisplayName("getHousesTable Tests")
    class GetHousesTableTests {

        @Test
        @DisplayName("Should return paginated houses")
        void getHousesTable_shouldReturnPaginatedHouses() {
            HouseRequestTable requestTable = new HouseRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            Page<House> housePage = new PageImpl<>(Collections.singletonList(testHouse));
            Page<HouseResponseTable> responsePage = new PageImpl<>(Collections.singletonList(new HouseResponseTable()));

            when(houseRepository.findHousesWithFilters(requestTable)).thenReturn(housePage);
            when(houseMapper.toResponseTablePage(housePage)).thenReturn(responsePage);

            Page<HouseResponseTable> result = houseService.getHousesTable(requestTable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getHouses Tests")
    class GetHousesTests {

        @Test
        @DisplayName("Should return houses page")
        void getHouses_shouldReturnHousesPage() {
            HouseRequestTable requestTable = new HouseRequestTable();
            Page<House> housePage = new PageImpl<>(Collections.singletonList(testHouse));

            when(houseRepository.findHousesWithFilters(requestTable)).thenReturn(housePage);

            Page<House> result = houseService.getHouses(requestTable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findByStreetId Tests")
    class FindByStreetIdTests {

        @Test
        @DisplayName("Should return houses by street id")
        void findByStreetId_shouldReturnHouses() {
            List<House> houses = Collections.singletonList(testHouse);
            List<HouseResponse> responses = Collections.singletonList(testHouseResponse);

            when(houseRepository.findByStreetId("street-id-123")).thenReturn(houses);
            when(houseMapper.toResponseList(houses)).thenReturn(responses);

            List<HouseResponse> result = houseService.findByStreetId("street-id-123");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("searchByStreetAndNumber Tests")
    class SearchByStreetAndNumberTests {

        @Test
        @DisplayName("Should return all houses for street when number is null")
        void searchByStreetAndNumber_shouldReturnAll_whenNumberIsNull() {
            List<House> houses = Collections.singletonList(testHouse);
            List<HouseResponse> responses = Collections.singletonList(testHouseResponse);

            when(houseRepository.findByStreetId("street-id-123")).thenReturn(houses);
            when(houseMapper.toResponseList(houses)).thenReturn(responses);

            List<HouseResponse> result = houseService.searchByStreetAndNumber("street-id-123", null);

            assertThat(result).hasSize(1);
            verify(houseRepository).findByStreetId("street-id-123");
        }

        @Test
        @DisplayName("Should return all houses for street when number is empty")
        void searchByStreetAndNumber_shouldReturnAll_whenNumberIsEmpty() {
            List<House> houses = Collections.singletonList(testHouse);
            List<HouseResponse> responses = Collections.singletonList(testHouseResponse);

            when(houseRepository.findByStreetId("street-id-123")).thenReturn(houses);
            when(houseMapper.toResponseList(houses)).thenReturn(responses);

            List<HouseResponse> result = houseService.searchByStreetAndNumber("street-id-123", "   ");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should search houses by street and number")
        void searchByStreetAndNumber_shouldSearch() {
            List<House> houses = Collections.singletonList(testHouse);
            List<HouseResponse> responses = Collections.singletonList(testHouseResponse);

            when(houseRepository.findByStreetIdAndHouseNumberContainingIgnoreCase("street-id-123", "42")).thenReturn(houses);
            when(houseMapper.toResponseList(houses)).thenReturn(responses);

            List<HouseResponse> result = houseService.searchByStreetAndNumber("street-id-123", "  42  ");

            assertThat(result).hasSize(1);
            verify(houseRepository).findByStreetIdAndHouseNumberContainingIgnoreCase("street-id-123", "42");
        }
    }
}
