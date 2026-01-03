package org.spacelab.housingutilitiessystemadmin.service;

import lombok.AllArgsConstructor;
import org.spacelab.housingutilitiessystemadmin.entity.ContactSection;
import org.spacelab.housingutilitiessystemadmin.repository.ContactSectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
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