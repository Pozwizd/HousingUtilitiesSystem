package org.spacelab.housingutilitiessystemadmin.service;

import lombok.AllArgsConstructor;
import org.spacelab.housingutilitiessystemadmin.entity.Contact;
import org.spacelab.housingutilitiessystemadmin.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public Contact save(Contact contact) {
        return contactRepository.save(contact);
    }

    public Optional<Contact> findById(String id) {
        return contactRepository.findById(id);
    }

    public List<Contact> findAll() {
        return contactRepository.findAll();
    }

    public void deleteById(String id) {
        contactRepository.deleteById(id);
    }

    public Contact update(Contact contact) {
        return contactRepository.save(contact);
    }

    public List<Contact> saveAll(List<Contact> contacts) {
        return contactRepository.saveAll(contacts);
    }

    public void deleteAll() {
        contactRepository.deleteAll();
    }
}