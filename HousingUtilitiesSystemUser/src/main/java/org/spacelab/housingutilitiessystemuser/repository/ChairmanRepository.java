package org.spacelab.housingutilitiessystemuser.repository;

import org.spacelab.housingutilitiessystemuser.entity.Chairman;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChairmanRepository extends MongoRepository<Chairman, String> {
    Optional<Chairman> findByHouseId(String houseId);
}
