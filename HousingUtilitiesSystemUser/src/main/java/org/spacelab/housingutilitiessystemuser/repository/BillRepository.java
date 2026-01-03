package org.spacelab.housingutilitiessystemuser.repository;

import org.spacelab.housingutilitiessystemuser.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends MongoRepository<Bill, String> {
    Page<Bill> findByIdIn(List<String> ids, Pageable pageable);

    List<Bill> findByIdIn(List<String> ids);
}
