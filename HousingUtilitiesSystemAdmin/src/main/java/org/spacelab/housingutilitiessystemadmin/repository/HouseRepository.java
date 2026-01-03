package org.spacelab.housingutilitiessystemadmin.repository;

import org.spacelab.housingutilitiessystemadmin.entity.location.House;
import org.spacelab.housingutilitiessystemadmin.repository.custom.HouseRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseRepository extends MongoRepository<House, String>, HouseRepositoryCustom {
    @Query("{ 'streetId': '?0' }")
    List<House> findByStreetId(String streetId);
    
    @Query("{ 'streetId': '?0', 'houseNumber': { $regex: '?1', $options: 'i' } }")
    List<House> findByStreetIdAndHouseNumberContainingIgnoreCase(String streetId, String houseNumber);
}