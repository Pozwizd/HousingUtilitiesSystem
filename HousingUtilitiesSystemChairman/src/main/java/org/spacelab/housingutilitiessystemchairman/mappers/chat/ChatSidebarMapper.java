package org.spacelab.housingutilitiessystemchairman.mappers.chat;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.models.chat.ChatContactResponse;
@Mapper(componentModel = "spring")
public interface ChatSidebarMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", expression = "java(user.getFullName())")
    @Mapping(target = "avatar", source = "photo")
    @Mapping(target = "online", source = "online")
    @Mapping(target = "participantType", constant = "USER")
    ChatContactResponse mapToContactResponse(User user);
}
