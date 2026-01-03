package org.spacelab.housingutilitiessystemuser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemuser.entity.Bill;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.repository.BillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillService Tests")
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillService billService;

    private User testUser;
    private Bill testBill;

    @BeforeEach
    void setUp() {
        testBill = new Bill();
        testBill.setId("bill-id");
        testBill.setBillNumber("BILL-001");
        testBill.setDate(LocalDate.now());

        testUser = new User();
        testUser.setId("user-id");
        testUser.setEmail("test@test.com");
        testUser.setBills(new ArrayList<>(List.of(testBill)));
    }

    @Nested
    @DisplayName("Find By User With Filters")
    class FindByUserWithFilters {
        @Test
        @DisplayName("Should return bills for user without filters")
        void findByUserWithFilters_shouldReturnBills() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getBillNumber()).isEqualTo("BILL-001");
        }

        @Test
        @DisplayName("Should return empty page for null user")
        void findByUserWithFilters_shouldReturnEmptyForNullUser() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(null, null, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty page for user without bills")
        void findByUserWithFilters_shouldReturnEmptyForNoBills() {
            testUser.setBills(null);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty page for user with empty bills list")
        void findByUserWithFilters_shouldReturnEmptyForEmptyBillsList() {
            testUser.setBills(new ArrayList<>());
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should filter by bill number")
        void findByUserWithFilters_shouldFilterByBillNumber() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, "BILL", null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by bill number case insensitive")
        void findByUserWithFilters_shouldFilterByBillNumberCaseInsensitive() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, "bill", null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter out non-matching bill number")
        void findByUserWithFilters_shouldFilterOutNonMatching() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, "INVOICE", null, null, pageable);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty string bill number filter")
        void findByUserWithFilters_shouldHandleEmptyBillNumber() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, "   ", null, null, pageable);

            assertThat(result.getContent()).hasSize(1); // Empty string should not filter
        }

        @Test
        @DisplayName("Should handle bill with null billNumber")
        void findByUserWithFilters_shouldHandleNullBillNumber() {
            testBill.setBillNumber(null);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, "BILL", null, null, pageable);

            assertThat(result.getContent()).isEmpty(); // Should filter out null billNumber
        }

        @Test
        @DisplayName("Should filter by start date")
        void findByUserWithFilters_shouldFilterByStartDate() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, startDate, null, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter out before start date")
        void findByUserWithFilters_shouldFilterOutBeforeStartDate() {
            LocalDate startDate = LocalDate.now().plusDays(1);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, startDate, null, pageable);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should filter by end date")
        void findByUserWithFilters_shouldFilterByEndDate() {
            LocalDate endDate = LocalDate.now().plusDays(1);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, null, endDate, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter out after end date")
        void findByUserWithFilters_shouldFilterOutAfterEndDate() {
            LocalDate endDate = LocalDate.now().minusDays(1);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, null, endDate, pageable);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should filter by date range")
        void findByUserWithFilters_shouldFilterByDateRange() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now().plusDays(1);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, startDate, endDate, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle bill with null date")
        void findByUserWithFilters_shouldHandleNullDate() {
            testBill.setDate(null);
            LocalDate startDate = LocalDate.now().minusDays(7);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, startDate, null, pageable);

            // Null date bills should pass date filters (no comparison possible)
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty when start offset exceeds size")
        void findByUserWithFilters_shouldReturnEmptyWhenStartExceedsSize() {
            Pageable pageable = PageRequest.of(10, 10); // Offset 100, but only 1 bill

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return properly paged results")
        void findByUserWithFilters_shouldReturnPagedResults() {
            // Add more bills
            Bill bill2 = new Bill();
            bill2.setId("bill-2");
            bill2.setBillNumber("BILL-002");
            bill2.setDate(LocalDate.now().minusDays(1));

            Bill bill3 = new Bill();
            bill3.setId("bill-3");
            bill3.setBillNumber("BILL-003");
            bill3.setDate(LocalDate.now().minusDays(2));

            testUser.setBills(new ArrayList<>(List.of(testBill, bill2, bill3)));
            Pageable pageable = PageRequest.of(0, 2);

            Page<Bill> result = billService.findByUserWithFilters(testUser, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should apply combined filters")
        void findByUserWithFilters_shouldApplyCombinedFilters() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now().plusDays(1);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Bill> result = billService.findByUserWithFilters(testUser, "BILL", startDate, endDate, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find By Id")
    class FindById {
        @Test
        @DisplayName("Should find bill by id")
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
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save bill")
        void save_shouldSaveBill() {
            when(billRepository.save(testBill)).thenReturn(testBill);

            Bill result = billService.save(testBill);

            assertThat(result).isNotNull();
            verify(billRepository).save(testBill);
        }

        @Test
        @DisplayName("Should save all bills")
        void saveAll_shouldSaveAllBills() {
            List<Bill> bills = List.of(testBill);
            when(billRepository.saveAll(bills)).thenReturn(bills);

            List<Bill> result = billService.saveAll(bills);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Generate Excel")
    class GenerateExcel {
        @Test
        @DisplayName("Should generate excel for bills")
        void generateExcel_shouldGenerateBytes() throws IOException {
            byte[] result = billService.generateExcel(List.of(testBill));

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should generate empty excel for empty list")
        void generateExcel_shouldGenerateEmptyExcel() throws IOException {
            byte[] result = billService.generateExcel(List.of());

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void generateExcel_shouldHandleNullValues() throws IOException {
            Bill nullBill = new Bill();
            nullBill.setBillNumber(null);
            nullBill.setDate(null);

            byte[] result = billService.generateExcel(List.of(nullBill));

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should generate excel with multiple bills")
        void generateExcel_shouldHandleMultipleBills() throws IOException {
            Bill bill2 = new Bill();
            bill2.setId("bill-2");
            bill2.setBillNumber("BILL-002");
            bill2.setDate(LocalDate.now().minusDays(1));

            byte[] result = billService.generateExcel(List.of(testBill, bill2));

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Get Filtered Bills")
    class GetFilteredBills {
        @Test
        @DisplayName("Should get filtered bills without pagination")
        void getFilteredBills_shouldReturnFilteredBills() {
            List<Bill> result = billService.getFilteredBills(testUser, null, null, null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty for null user")
        void getFilteredBills_shouldReturnEmptyForNullUser() {
            List<Bill> result = billService.getFilteredBills(null, null, null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for user with null bills")
        void getFilteredBills_shouldReturnEmptyForNullBills() {
            testUser.setBills(null);
            List<Bill> result = billService.getFilteredBills(testUser, null, null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for user with empty bills")
        void getFilteredBills_shouldReturnEmptyForEmptyBills() {
            testUser.setBills(new ArrayList<>());
            List<Bill> result = billService.getFilteredBills(testUser, null, null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter by bill number")
        void getFilteredBills_shouldFilterByBillNumber() {
            List<Bill> result = billService.getFilteredBills(testUser, "BILL", null, null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter out non-matching")
        void getFilteredBills_shouldFilterOutNonMatching() {
            List<Bill> result = billService.getFilteredBills(testUser, "INVOICE", null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null bill number in entity")
        void getFilteredBills_shouldHandleNullBillNumberInEntity() {
            testBill.setBillNumber(null);
            List<Bill> result = billService.getFilteredBills(testUser, "BILL", null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter by start date")
        void getFilteredBills_shouldFilterByStartDate() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            List<Bill> result = billService.getFilteredBills(testUser, null, startDate, null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by end date")
        void getFilteredBills_shouldFilterByEndDate() {
            LocalDate endDate = LocalDate.now().plusDays(1);
            List<Bill> result = billService.getFilteredBills(testUser, null, null, endDate);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should handle empty string filter")
        void getFilteredBills_shouldHandleEmptyStringFilter() {
            List<Bill> result = billService.getFilteredBills(testUser, "   ", null, null);

            assertThat(result).hasSize(1);
        }
    }
}
