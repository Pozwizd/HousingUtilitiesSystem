package org.spacelab.housingutilitiessystemchairman.repository.mongo;

import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemchairman.entity.location.City;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CityRepository extends MongoRepository<City, ObjectId> {
    List<City> findByNameContainingIgnoreCase(String name);
}
