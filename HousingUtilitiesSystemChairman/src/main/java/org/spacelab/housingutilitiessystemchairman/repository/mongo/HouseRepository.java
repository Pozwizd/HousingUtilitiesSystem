package org.spacelab.housingutilitiessystemchairman.repository.mongo;

import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.custom.HouseRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
@Repository
public interface HouseRepository extends MongoRepository<House, String>, HouseRepositoryCustom {
    @Query("{ 'streetId': '?0' }")
    List<House> findByStreetId(String streetId);
    @Query("{ 'streetId': '?0', 'houseNumber': { $regex: '?1', $options: 'i' } }")
    List<House> findByStreetIdAndHouseNumberContainingIgnoreCase(String streetId, String houseNumber);
    @Query(value = "chairman")
    Set<House> findByChairman(Chairman chairman);
}
