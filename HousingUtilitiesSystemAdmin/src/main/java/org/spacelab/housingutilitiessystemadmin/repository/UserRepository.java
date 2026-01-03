package org.spacelab.housingutilitiessystemadmin.repository;

import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.entity.location.House;
import org.spacelab.housingutilitiessystemadmin.repository.custom.UserRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    List<User> findByHouse(House house);
    Page<User> findByHouse(House house, Pageable pageable);
}
