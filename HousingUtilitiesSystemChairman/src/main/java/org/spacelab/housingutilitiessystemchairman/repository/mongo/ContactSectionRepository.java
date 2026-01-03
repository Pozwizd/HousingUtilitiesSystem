package org.spacelab.housingutilitiessystemchairman.repository.mongo;
import org.spacelab.housingutilitiessystemchairman.entity.ContactSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ContactSectionRepository extends MongoRepository<ContactSection, String> {
}
