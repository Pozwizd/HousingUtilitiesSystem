package org.spacelab.housingutilitiessystemchairman.mappers;

import org.mapstruct.*;
import org.spacelab.housingutilitiessystemchairman.entity.Contact;
import org.spacelab.housingutilitiessystemchairman.entity.ContactSection;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactRequest;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactResponse;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactSectionRequest;
import org.spacelab.housingutilitiessystemchairman.models.contact.ContactSectionResponse;

import java.util.List;
@Mapper(componentModel = "spring")
public interface ContactMapper {
    ContactResponse toResponse(Contact contact);
    List<ContactResponse> toResponseList(List<Contact> contacts);
    @Mapping(target = "contacts", source = "contacts")
    ContactSectionResponse toSectionResponse(ContactSection section);
    List<ContactSectionResponse> toSectionResponseList(List<ContactSection> sections);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoPath", ignore = true)
    Contact toEntity(ContactRequest request);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    ContactSection toSectionEntity(ContactSectionRequest request);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoPath", ignore = true)
    void partialUpdate(ContactRequest request, @MappingTarget Contact contact);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    void partialUpdateSection(ContactSectionRequest request, @MappingTarget ContactSection section);
}
