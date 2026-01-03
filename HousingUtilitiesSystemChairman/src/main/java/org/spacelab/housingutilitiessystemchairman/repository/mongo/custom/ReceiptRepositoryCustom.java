package org.spacelab.housingutilitiessystemchairman.repository.mongo.custom;
import org.spacelab.housingutilitiessystemchairman.entity.Receipt;
import org.spacelab.housingutilitiessystemchairman.models.filters.receipt.ReceiptRequestTable;
import org.springframework.data.domain.Page;
public interface ReceiptRepositoryCustom {
    Page<Receipt> findReceiptsWithFilters(ReceiptRequestTable filter);
}
