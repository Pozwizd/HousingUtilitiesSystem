package org.spacelab.housingutilitiessystemchairman.repository.mongo;
import org.spacelab.housingutilitiessystemchairman.entity.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
}
