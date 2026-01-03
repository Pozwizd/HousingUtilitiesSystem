package org.spacelab.housingutilitiessystemchairman.controller.contacts;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactRequest;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactResponse;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactSectionRequest;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactSectionResponse;
import org.spacelab.housingutilitiessystemchairman.service.ContactService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/contacts")
@AllArgsConstructor
@Slf4j
public class ContactRestController {
    private final ContactService contactService;
    @GetMapping("/sections")
    public ResponseEntity<List<ContactSectionResponse>> getAllSections() {
        return ResponseEntity.ok(contactService.getAllSections());
    }
    @GetMapping("/sections/{id}")
    public ResponseEntity<ContactSectionResponse> getSection(@PathVariable String id) {
        return ResponseEntity.ok(contactService.getSectionById(id));
    }

    @PostMapping(value = "/sections", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContactSectionResponse> createSection(
            @RequestPart("section") @Valid ContactSectionRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) throws IOException {
        return ResponseEntity.ok(contactService.createSection(request, photos));
    }

    @PutMapping(value = "/sections/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContactSectionResponse> updateSection(
            @PathVariable String id,
            @RequestPart("section") @Valid ContactSectionRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) throws IOException {
        return ResponseEntity.ok(contactService.updateSection(id, request, photos));
    }
    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Boolean> deleteSection(@PathVariable String id) {
        contactService.deleteSection(id);
        return ResponseEntity.ok(true);
    }
    @GetMapping("/all")
    public ResponseEntity<List<ContactResponse>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContact(@PathVariable String id) {
        return ResponseEntity.ok(contactService.getContactById(id));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContactResponse> createContact(
            @ModelAttribute @Valid ContactRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        return ResponseEntity.ok(contactService.createContact(request, photo));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContactResponse> updateContact(
            @PathVariable String id,
            @ModelAttribute @Valid ContactRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        return ResponseEntity.ok(contactService.updateContact(id, request, photo));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteContact(@PathVariable String id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok(true);
    }
}
