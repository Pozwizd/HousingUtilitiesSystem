package org.spacelab.housingutilitiessystemchairman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.Bill;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.BillRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillService Tests")
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillService billService;

    private Bill testBill;

    @BeforeEach
    void setUp() {
        testBill = new Bill();
        testBill.setId("bill-id");
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        @Test
        @DisplayName("Should save bill")
        void save_shouldSaveBill() {
            when(billRepository.save(any(Bill.class))).thenReturn(testBill);
            Bill result = billService.save(testBill);
            assertThat(result).isNotNull();
            verify(billRepository).save(testBill);
        }

        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnBill() {
            when(billRepository.findById("bill-id")).thenReturn(Optional.of(testBill));
            Optional<Bill> result = billService.findById("bill-id");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when not found")
        void findById_shouldReturnEmpty() {
            when(billRepository.findById("unknown")).thenReturn(Optional.empty());
            Optional<Bill> result = billService.findById("unknown");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all bills")
        void findAll_shouldReturnAllBills() {
            when(billRepository.findAll()).thenReturn(Arrays.asList(testBill, new Bill()));
            List<Bill> result = billService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete by id")
        void deleteById_shouldDeleteBill() {
            doNothing().when(billRepository).deleteById("bill-id");
            billService.deleteById("bill-id");
            verify(billRepository).deleteById("bill-id");
        }

        @Test
        @DisplayName("Should update bill")
        void update_shouldUpdateBill() {
            when(billRepository.save(any(Bill.class))).thenReturn(testBill);
            Bill result = billService.update(testBill);
            assertThat(result).isNotNull();
            verify(billRepository).save(testBill);
        }

        @Test
        @DisplayName("Should save all bills")
        void saveAll_shouldSaveAllBills() {
            List<Bill> bills = Arrays.asList(testBill, new Bill());
            when(billRepository.saveAll(bills)).thenReturn(bills);
            List<Bill> result = billService.saveAll(bills);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllBills() {
            doNothing().when(billRepository).deleteAll();
            billService.deleteAll();
            verify(billRepository).deleteAll();
        }
    }
}
