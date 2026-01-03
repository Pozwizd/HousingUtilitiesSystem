package org.spacelab.housingutilitiessystemchairman.service;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemchairman.entity.Receipt;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.filters.receipt.ReceiptRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.receipt.ReceiptResponseTable;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ReceiptRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
@Service
@RequiredArgsConstructor
public class ReceiptService {
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final ReceiptRepository receiptRepository;
    public PageResponse<ReceiptResponseTable> getReceiptsTable(ReceiptRequestTable requestTable) {
        Page<Receipt> receiptsPage = receiptRepository.findReceiptsWithFilters(requestTable);
        Page<ReceiptResponseTable> mappedPage = receiptsPage.map(this::mapToResponse);
        return PageResponse.of(mappedPage);
    }
    private ReceiptResponseTable mapToResponse(Receipt receipt) {
        String billNumber = receipt.getBill() != null ? receipt.getBill().getId() : "-";
        String formattedDate = receipt.getLocalDate() != null ? receipt.getLocalDate().format(DISPLAY_DATE_FORMATTER) : "-";
        return ReceiptResponseTable.builder()
                .receiptNumber(receipt.getId())
                .billNumber(billNumber)
                .formattedDate(formattedDate)
                .build();
    }
}
