package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.Bill;
import org.spacelab.housingutilitiessystemadmin.repository.BillRepository;

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
        testBill.setId("bill-id-123");
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {
        @Test
        @DisplayName("Should save and return bill")
        void save_shouldSaveAndReturnBill() {
            when(billRepository.save(any(Bill.class))).thenReturn(testBill);

            Bill result = billService.save(testBill);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("bill-id-123");
            verify(billRepository).save(testBill);
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {
        @Test
        @DisplayName("Should return Optional with bill when found")
        void findById_shouldReturnOptionalWithBill() {
            when(billRepository.findById("bill-id-123")).thenReturn(Optional.of(testBill));

            Optional<Bill> result = billService.findById("bill-id-123");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("bill-id-123");
        }

        @Test
        @DisplayName("Should return empty Optional when not found")
        void findById_shouldReturnEmptyOptional() {
            when(billRepository.findById("nonexistent")).thenReturn(Optional.empty());

            Optional<Bill> result = billService.findById("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {
        @Test
        @DisplayName("Should return all bills")
        void findAll_shouldReturnAllBills() {
            Bill bill2 = new Bill();
            bill2.setId("bill-id-456");
            List<Bill> bills = Arrays.asList(testBill, bill2);
            when(billRepository.findAll()).thenReturn(bills);

            List<Bill> result = billService.findAll();

            assertThat(result).hasSize(2);
            verify(billRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no bills")
        void findAll_shouldReturnEmptyList() {
            when(billRepository.findAll()).thenReturn(List.of());

            List<Bill> result = billService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {
        @Test
        @DisplayName("Should delete bill by id")
        void deleteById_shouldDeleteBill() {
            doNothing().when(billRepository).deleteById("bill-id-123");

            billService.deleteById("bill-id-123");

            verify(billRepository).deleteById("bill-id-123");
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update and return bill")
        void update_shouldUpdateAndReturnBill() {
            when(billRepository.save(any(Bill.class))).thenReturn(testBill);

            Bill result = billService.update(testBill);

            assertThat(result).isNotNull();
            verify(billRepository).save(testBill);
        }
    }

    @Nested
    @DisplayName("saveAll Tests")
    class SaveAllTests {
        @Test
        @DisplayName("Should save all bills")
        void saveAll_shouldSaveAllBills() {
            Bill bill2 = new Bill();
            bill2.setId("bill-id-456");
            List<Bill> bills = Arrays.asList(testBill, bill2);
            when(billRepository.saveAll(bills)).thenReturn(bills);

            List<Bill> result = billService.saveAll(bills);

            assertThat(result).hasSize(2);
            verify(billRepository).saveAll(bills);
        }
    }

    @Nested
    @DisplayName("deleteAll Tests")
    class DeleteAllTests {
        @Test
        @DisplayName("Should delete all bills")
        void deleteAll_shouldDeleteAllBills() {
            doNothing().when(billRepository).deleteAll();

            billService.deleteAll();

            verify(billRepository).deleteAll();
        }
    }
}
