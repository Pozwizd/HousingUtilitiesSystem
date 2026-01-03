package org.spacelab.housingutilitiessystemchairman.repository.mongo;
import org.spacelab.housingutilitiessystemchairman.entity.location.Region;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface RegionRepository extends MongoRepository<Region, String> {
}
