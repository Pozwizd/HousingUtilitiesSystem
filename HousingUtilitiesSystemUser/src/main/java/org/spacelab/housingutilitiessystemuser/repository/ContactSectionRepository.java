package org.spacelab.housingutilitiessystemuser.repository;

import org.spacelab.housingutilitiessystemuser.entity.ContactSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactSectionRepository extends MongoRepository<ContactSection, String> {
}
