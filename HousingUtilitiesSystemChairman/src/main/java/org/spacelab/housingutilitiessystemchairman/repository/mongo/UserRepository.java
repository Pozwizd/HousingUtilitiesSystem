package org.spacelab.housingutilitiessystemchairman.repository.mongo;

import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.custom.UserRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends MongoRepository<User, ObjectId>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    Optional<User> findByLogin(String login);
    List<User> findByHouse(House house);
    Page<User> findByHouse(House house, Pageable pageable);

    List<User> findByHouseIn(List<House> houses);
}
