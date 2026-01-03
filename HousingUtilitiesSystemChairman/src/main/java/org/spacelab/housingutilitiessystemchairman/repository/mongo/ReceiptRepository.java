package org.spacelab.housingutilitiessystemchairman.repository.mongo;

import org.spacelab.housingutilitiessystemchairman.entity.Receipt;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.custom.ReceiptRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ReceiptRepository extends MongoRepository<Receipt, String>, ReceiptRepositoryCustom {
    List<Receipt> findAllByOrderByLocalDateDesc();
}
