package org.spacelab.housingutilitiessystemchairman.service;
import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemchairman.entity.Contact;
import org.spacelab.housingutilitiessystemchairman.entity.ContactSection;
import org.spacelab.housingutilitiessystemchairman.mappers.ContactMapper;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactRequest;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactResponse;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactSectionRequest;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactSectionResponse;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ContactRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ContactSectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
    private final ContactSectionRepository contactSectionRepository;
    private final ContactMapper contactMapper;
    private final FileService fileService;
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
    public ContactResponse getContactById(String id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found: " + id));
        return contactMapper.toResponse(contact);
    }
    public List<ContactResponse> getAllContacts() {
        return contactMapper.toResponseList(contactRepository.findAll());
    }

    public ContactResponse createContact(ContactRequest request, MultipartFile photo) throws IOException {
        Contact contact = contactMapper.toEntity(request);
        if (photo != null && !photo.isEmpty()) {
            String photoPath = fileService.uploadFile(photo);
            contact.setPhotoPath(photoPath);
        }
        Contact saved = contactRepository.save(contact);
        return contactMapper.toResponse(saved);
    }

    public ContactResponse updateContact(String id, ContactRequest request, MultipartFile photo) throws IOException {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found: " + id));
        contactMapper.partialUpdate(request, contact);
        if (photo != null && !photo.isEmpty()) {
            if (contact.getPhotoPath() != null) {
                fileService.deleteFile(contact.getPhotoPath());
            }
            String photoPath = fileService.uploadFile(photo);
            contact.setPhotoPath(photoPath);
        }
        Contact saved = contactRepository.save(contact);
        return contactMapper.toResponse(saved);
    }
    public List<ContactSectionResponse> getAllSections() {
        List<ContactSection> sections = contactSectionRepository.findAll();
        return contactMapper.toSectionResponseList(sections);
    }
    public ContactSectionResponse getSectionById(String id) {
        ContactSection section = contactSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContactSection not found: " + id));
        return contactMapper.toSectionResponse(section);
    }

    public ContactSectionResponse createSection(ContactSectionRequest request, List<MultipartFile> photos)
            throws IOException {
        ContactSection section = contactMapper.toSectionEntity(request);
        if (request.getContacts() != null && !request.getContacts().isEmpty()) {
            List<Contact> contacts = new ArrayList<>();
            for (int i = 0; i < request.getContacts().size(); i++) {
                ContactRequest contactRequest = request.getContacts().get(i);
                Contact contact = contactMapper.toEntity(contactRequest);
                if (contactRequest.getPhotoIndex() != null && photos != null &&
                        contactRequest.getPhotoIndex() < photos.size()) {
                    MultipartFile photo = photos.get(contactRequest.getPhotoIndex());
                    if (photo != null && !photo.isEmpty()) {
                        String photoPath = fileService.uploadFile(photo);
                        contact.setPhotoPath(photoPath);
                    }
                }
                contacts.add(contactRepository.save(contact));
            }
            section.setContacts(contacts);
        } else {
            section.setContacts(new ArrayList<>());
        }
        ContactSection saved = contactSectionRepository.save(section);
        return contactMapper.toSectionResponse(saved);
    }

    public ContactSectionResponse updateSection(String id, ContactSectionRequest request, List<MultipartFile> photos)
            throws IOException {
        ContactSection section = contactSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContactSection not found: " + id));
        contactMapper.partialUpdateSection(request, section);
        if (request.getContacts() != null) {
            List<Contact> existingContacts = section.getContacts() != null
                    ? section.getContacts()
                    : new ArrayList<>();
            List<String> requestContactIds = request.getContacts().stream()
                    .map(ContactRequest::getId)
                    .filter(contactId -> contactId != null && !contactId.isEmpty())
                    .collect(Collectors.toList());
            for (Contact c : existingContacts) {
                if (c.getId() != null && !requestContactIds.contains(c.getId())) {
                    if (c.getPhotoPath() != null) {
                        fileService.deleteFile(c.getPhotoPath());
                    }
                    contactRepository.deleteById(c.getId());
                }
            }
            List<Contact> updatedContacts = new ArrayList<>();
            for (ContactRequest contactRequest : request.getContacts()) {
                if (contactRequest.getId() != null && !contactRequest.getId().isEmpty()) {
                    Optional<Contact> existingContact = contactRepository.findById(contactRequest.getId());
                    if (existingContact.isPresent()) {
                        Contact contact = existingContact.get();
                        contactMapper.partialUpdate(contactRequest, contact);
                        if (contactRequest.getPhotoIndex() != null && photos != null &&
                                contactRequest.getPhotoIndex() < photos.size()) {
                            MultipartFile photo = photos.get(contactRequest.getPhotoIndex());
                            if (photo != null && !photo.isEmpty()) {
                                if (contact.getPhotoPath() != null) {
                                    fileService.deleteFile(contact.getPhotoPath());
                                }
                                String photoPath = fileService.uploadFile(photo);
                                contact.setPhotoPath(photoPath);
                            }
                        }
                        updatedContacts.add(contactRepository.save(contact));
                    } else {
                        Contact newContact = contactMapper.toEntity(contactRequest);
                        handlePhotoUpload(newContact, contactRequest, photos);
                        updatedContacts.add(contactRepository.save(newContact));
                    }
                } else {
                    Contact newContact = contactMapper.toEntity(contactRequest);
                    handlePhotoUpload(newContact, contactRequest, photos);
                    updatedContacts.add(contactRepository.save(newContact));
                }
            }
            section.setContacts(updatedContacts);
        }
        ContactSection saved = contactSectionRepository.save(section);
        return contactMapper.toSectionResponse(saved);
    }

    private void handlePhotoUpload(Contact contact, ContactRequest request, List<MultipartFile> photos)
            throws IOException {
        if (request.getPhotoIndex() != null && photos != null &&
                request.getPhotoIndex() < photos.size()) {
            MultipartFile photo = photos.get(request.getPhotoIndex());
            if (photo != null && !photo.isEmpty()) {
                String photoPath = fileService.uploadFile(photo);
                contact.setPhotoPath(photoPath);
            }
        }
    }
    public void deleteSection(String id) {
        ContactSection section = contactSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContactSection not found: " + id));
        if (section.getContacts() != null) {
            for (Contact c : section.getContacts()) {
                if (c.getPhotoPath() != null) {
                    try {
                        fileService.deleteFile(c.getPhotoPath());
                    } catch (IOException e) {
                    }
                }
                contactRepository.deleteById(c.getId());
            }
        }
        contactSectionRepository.deleteById(id);
    }
    public void deleteContact(String id) {
        Contact contact = contactRepository.findById(id).orElse(null);
        if (contact != null && contact.getPhotoPath() != null) {
            try {
                fileService.deleteFile(contact.getPhotoPath());
            } catch (IOException e) {
            }
        }
        contactRepository.deleteById(id);
    }
}
