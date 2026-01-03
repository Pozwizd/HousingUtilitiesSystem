package org.spacelab.housingutilitiessystemuser.repository;

import org.spacelab.housingutilitiessystemuser.entity.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
}
