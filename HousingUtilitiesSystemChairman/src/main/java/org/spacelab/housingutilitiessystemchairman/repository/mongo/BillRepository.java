package org.spacelab.housingutilitiessystemchairman.repository.mongo;
import org.spacelab.housingutilitiessystemchairman.entity.Bill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface BillRepository extends MongoRepository<Bill, String> {
}
