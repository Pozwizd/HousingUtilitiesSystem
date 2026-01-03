package org.spacelab.housingutilitiessystemchairman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.location.Region;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.RegionRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionService Tests")
class RegionServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionService regionService;

    private Region testRegion;

    @BeforeEach
    void setUp() {
        testRegion = new Region();
        testRegion.setId("region-id");
        testRegion.setName("Test Region");
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        @Test
        @DisplayName("Should save region")
        void save_shouldSaveRegion() {
            when(regionRepository.save(any(Region.class))).thenReturn(testRegion);
            Region result = regionService.save(testRegion);
            assertThat(result).isNotNull();
            verify(regionRepository).save(testRegion);
        }

        @Test
        @DisplayName("Should save all regions with Iterable")
        void saveAll_iterable_shouldSaveAllRegions() {
            List<Region> regions = Arrays.asList(testRegion, new Region());
            when(regionRepository.saveAll(any(Iterable.class))).thenReturn(regions);
            List<Region> result = regionService.saveAll((Iterable<Region>) regions);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnRegion() {
            when(regionRepository.findById("region-id")).thenReturn(Optional.of(testRegion));
            Optional<Region> result = regionService.findById("region-id");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when not found")
        void findById_shouldReturnEmpty() {
            when(regionRepository.findById("unknown")).thenReturn(Optional.empty());
            Optional<Region> result = regionService.findById("unknown");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all regions")
        void findAll_shouldReturnAllRegions() {
            when(regionRepository.findAll()).thenReturn(Arrays.asList(testRegion, new Region()));
            List<Region> result = regionService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllRegions() {
            doNothing().when(regionRepository).deleteAll();
            regionService.deleteAll();
            verify(regionRepository).deleteAll();
        }

        @Test
        @DisplayName("Should delete region")
        void delete_shouldDeleteRegion() {
            doNothing().when(regionRepository).delete(testRegion);
            regionService.delete(testRegion);
            verify(regionRepository).delete(testRegion);
        }

        @Test
        @DisplayName("Should check if exists by id - true")
        void existsById_shouldReturnTrue() {
            when(regionRepository.existsById("region-id")).thenReturn(true);
            boolean result = regionService.existsById("region-id");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should check if exists by id - false")
        void existsById_shouldReturnFalse() {
            when(regionRepository.existsById("unknown")).thenReturn(false);
            boolean result = regionService.existsById("unknown");
            assertThat(result).isFalse();
        }
    }
}
