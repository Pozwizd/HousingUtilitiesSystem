package org.spacelab.housingutilitiessystemadmin.repository;

import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.entity.location.Region;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends MongoRepository<Region, ObjectId> {
}
