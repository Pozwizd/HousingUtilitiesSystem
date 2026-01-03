package org.spacelab.housingutilitiessystemchairman.service;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemchairman.entity.ContactSection;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ContactSectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ContactSectionService {
    private final ContactSectionRepository contactSectionRepository;
    public ContactSection save(ContactSection contactSection) {
        return contactSectionRepository.save(contactSection);
    }
    public Optional<ContactSection> findById(String id) {
        return contactSectionRepository.findById(id);
    }
    public List<ContactSection> findAll() {
        return contactSectionRepository.findAll();
    }
    public void deleteById(String id) {
        contactSectionRepository.deleteById(id);
    }
    public ContactSection update(ContactSection contactSection) {
        return contactSectionRepository.save(contactSection);
    }
    public List<ContactSection> saveAll(List<ContactSection> contactSections) {
        return contactSectionRepository.saveAll(contactSections);
    }
    public void deleteAll() {
        contactSectionRepository.deleteAll();
    }
}
