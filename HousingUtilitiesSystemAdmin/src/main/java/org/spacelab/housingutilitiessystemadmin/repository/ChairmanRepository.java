package org.spacelab.housingutilitiessystemadmin.repository;

import org.spacelab.housingutilitiessystemadmin.entity.Chairman;
import org.spacelab.housingutilitiessystemadmin.repository.custom.ChairmanRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChairmanRepository extends MongoRepository<Chairman, String>, ChairmanRepositoryCustom {
    Optional<Chairman> findByEmail(String email);
    Optional<Chairman> findByLogin(String login);
    
    @Query("{ $or: [ { 'firstName': { $regex: ?0, $options: 'i' } }, { 'middleName': { $regex: ?0, $options: 'i' } }, { 'lastName': { $regex: ?0, $options: 'i' } } ] }")
    List<Chairman> findByFullNameContaining(String name);
}