package org.spacelab.housingutilitiessystemchairman.models.filters.receipt;
import lombok.Data;
@Data
public class ReceiptRequestTable {
    private int page = 0;
    private int size = 10;
    private String receiptNumber;
    private String billNumber;
    private String date;
}
