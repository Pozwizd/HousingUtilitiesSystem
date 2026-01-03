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
import org.spacelab.housingutilitiessystemchairman.entity.Receipt;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.filters.receipt.ReceiptRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.receipt.ReceiptResponseTable;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ReceiptRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceiptService Tests")
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private ReceiptService receiptService;

    private Receipt testReceipt;
    private Bill testBill;

    @BeforeEach
    void setUp() {
        testBill = new Bill();
        testBill.setId("bill-id");

        testReceipt = new Receipt();
        testReceipt.setId("receipt-id");
        testReceipt.setBill(testBill);
        testReceipt.setLocalDate(LocalDate.of(2024, 1, 15));
    }

    @Nested
    @DisplayName("Table Operations")
    class TableOperations {
        @Test
        @DisplayName("Should get receipts table")
        void getReceiptsTable_shouldReturnTable() {
            ReceiptRequestTable requestTable = new ReceiptRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            Page<Receipt> receiptsPage = new PageImpl<>(List.of(testReceipt), PageRequest.of(0, 10), 1);

            when(receiptRepository.findReceiptsWithFilters(requestTable)).thenReturn(receiptsPage);

            PageResponse<ReceiptResponseTable> result = receiptService.getReceiptsTable(requestTable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should map receipt with bill to response")
        void getReceiptsTable_shouldMapWithBill() {
            ReceiptRequestTable requestTable = new ReceiptRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            Page<Receipt> receiptsPage = new PageImpl<>(List.of(testReceipt), PageRequest.of(0, 10), 1);

            when(receiptRepository.findReceiptsWithFilters(requestTable)).thenReturn(receiptsPage);

            PageResponse<ReceiptResponseTable> result = receiptService.getReceiptsTable(requestTable);

            assertThat(result.getContent().get(0).getBillNumber()).isEqualTo("bill-id");
            assertThat(result.getContent().get(0).getFormattedDate()).isEqualTo("15.01.2024");
        }

        @Test
        @DisplayName("Should handle receipt without bill")
        void getReceiptsTable_shouldHandleNullBill() {
            testReceipt.setBill(null);
            ReceiptRequestTable requestTable = new ReceiptRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            Page<Receipt> receiptsPage = new PageImpl<>(List.of(testReceipt), PageRequest.of(0, 10), 1);

            when(receiptRepository.findReceiptsWithFilters(requestTable)).thenReturn(receiptsPage);

            PageResponse<ReceiptResponseTable> result = receiptService.getReceiptsTable(requestTable);

            assertThat(result.getContent().get(0).getBillNumber()).isEqualTo("-");
        }

        @Test
        @DisplayName("Should handle receipt without date")
        void getReceiptsTable_shouldHandleNullDate() {
            testReceipt.setLocalDate(null);
            ReceiptRequestTable requestTable = new ReceiptRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            Page<Receipt> receiptsPage = new PageImpl<>(List.of(testReceipt), PageRequest.of(0, 10), 1);

            when(receiptRepository.findReceiptsWithFilters(requestTable)).thenReturn(receiptsPage);

            PageResponse<ReceiptResponseTable> result = receiptService.getReceiptsTable(requestTable);

            assertThat(result.getContent().get(0).getFormattedDate()).isEqualTo("-");
        }
    }
}
