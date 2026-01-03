package org.spacelab.housingutilitiessystemchairman.repository.mongo;

import org.spacelab.housingutilitiessystemchairman.entity.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {
    @Query("{email:'?0'}")
    Admin findByEmail(String email);
    @Query("{email: '?0'}")
    Optional<Admin> findOptByEmail(String email);
}
