package org.spacelab.housingutilitiessystemadmin.repository;

import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreetRepository extends MongoRepository<Street, String> {
    @Query("{ 'city._id': ?0 }")
    List<Street> findByCityId(String cityId);

    @Query("{ 'city._id': ?0, 'name': { $regex: ?1, $options: 'i' } }")
    List<Street> findByCityIdAndNameContainingIgnoreCase(String cityId, String name);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Street> findByNameContainingIgnoreCase(String name);

    List<Street> findByCity(City city);

    List<Street> findByCityAndNameContainingIgnoreCase(City city, String name);
}
