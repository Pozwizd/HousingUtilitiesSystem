package org.spacelab.housingutilitiessystemchairman.models.receipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponseTable {
    private String receiptNumber;
    private String billNumber;
    private String formattedDate;
}
