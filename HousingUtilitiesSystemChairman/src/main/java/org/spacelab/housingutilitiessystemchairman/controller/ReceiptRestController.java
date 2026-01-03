package org.spacelab.housingutilitiessystemchairman.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.filters.receipt.ReceiptRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.receipt.ReceiptResponseTable;
import org.spacelab.housingutilitiessystemchairman.service.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class ReceiptRestController {
    private final ReceiptService receiptService;
    @PostMapping("/getAll")
    public ResponseEntity<PageResponse<ReceiptResponseTable>> getReceipts(@Valid @RequestBody ReceiptRequestTable requestTable) {
        log.debug("Loading receipts page {} with size {}", requestTable.getPage(), requestTable.getSize());
        return ResponseEntity.ok(receiptService.getReceiptsTable(requestTable));
    }
}
