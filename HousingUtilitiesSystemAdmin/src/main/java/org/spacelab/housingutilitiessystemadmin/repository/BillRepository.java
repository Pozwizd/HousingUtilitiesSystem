package org.spacelab.housingutilitiessystemadmin.repository;

import org.spacelab.housingutilitiessystemadmin.entity.Bill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends MongoRepository<Bill, String> {
}