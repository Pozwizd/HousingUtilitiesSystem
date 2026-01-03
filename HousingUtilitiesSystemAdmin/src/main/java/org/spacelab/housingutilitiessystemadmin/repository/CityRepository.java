package org.spacelab.housingutilitiessystemadmin.repository;

import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends MongoRepository<City, ObjectId> {
    List<City> findByNameContainingIgnoreCase(String name);
}