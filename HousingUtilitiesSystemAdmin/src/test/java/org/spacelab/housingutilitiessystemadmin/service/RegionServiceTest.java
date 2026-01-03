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
import org.spacelab.housingutilitiessystemadmin.entity.location.Region;
import org.spacelab.housingutilitiessystemadmin.repository.RegionRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionService Tests")
class RegionServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionService regionService;

    private Region testRegion;
    private ObjectId testObjectId;

    @BeforeEach
    void setUp() {
        testObjectId = new ObjectId();
        testRegion = new Region();
        testRegion.setId(testObjectId.toString()); // Region.id is String
        testRegion.setName("Московская область");
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save region")
        void save_shouldSaveRegion() {
            when(regionRepository.save(testRegion)).thenReturn(testRegion);

            Region result = regionService.save(testRegion);

            assertThat(result).isEqualTo(testRegion);
            verify(regionRepository).save(testRegion);
        }
    }

    @Nested
    @DisplayName("saveAll Tests")
    class SaveAllTests {

        @Test
        @DisplayName("Should save all regions")
        void saveAll_shouldSaveAllRegions() {
            List<Region> regions = Arrays.asList(testRegion, new Region());
            when(regionRepository.saveAll(regions)).thenReturn(regions);

            List<Region> result = regionService.saveAll(regions);

            assertThat(result).hasSize(2);
            verify(regionRepository).saveAll(regions);
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find region by id")
        void findById_shouldReturnRegion() {
            // RegionService.findById takes ObjectId and passes it to repository  
            when(regionRepository.findById(testObjectId)).thenReturn(Optional.of(testRegion));

            Optional<Region> result = regionService.findById(testObjectId);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Московская область");
        }

        @Test
        @DisplayName("Should return empty when region not found")
        void findById_shouldReturnEmpty_whenNotFound() {
            when(regionRepository.findById(testObjectId)).thenReturn(Optional.empty());

            Optional<Region> result = regionService.findById(testObjectId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all regions")
        void findAll_shouldReturnAllRegions() {
            List<Region> regions = Arrays.asList(testRegion, new Region());
            when(regionRepository.findAll()).thenReturn(regions);

            List<Region> result = regionService.findAll();

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("deleteAll Tests")
    class DeleteAllTests {

        @Test
        @DisplayName("Should delete all regions")
        void deleteAll_shouldDeleteAllRegions() {
            regionService.deleteAll();

            verify(regionRepository).deleteAll();
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete region")
        void delete_shouldDeleteRegion() {
            regionService.delete(testRegion);

            verify(regionRepository).delete(testRegion);
        }
    }

    @Nested
    @DisplayName("existsById Tests")
    class ExistsByIdTests {

        @Test
        @DisplayName("Should return true when region exists")
        void existsById_shouldReturnTrue_whenExists() {
            // RegionService.existsById takes ObjectId
            when(regionRepository.existsById(testObjectId)).thenReturn(true);

            boolean result = regionService.existsById(testObjectId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when region does not exist")
        void existsById_shouldReturnFalse_whenNotExists() {
            when(regionRepository.existsById(testObjectId)).thenReturn(false);

            boolean result = regionService.existsById(testObjectId);

            assertThat(result).isFalse();
        }
    }
}
