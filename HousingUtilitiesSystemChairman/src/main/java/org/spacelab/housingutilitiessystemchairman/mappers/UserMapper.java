package org.spacelab.housingutilitiessystemchairman.mappers;

import org.mapstruct.*;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.user.UserRequest;
import org.spacelab.housingutilitiessystemchairman.models.user.UserResponse;
import org.spacelab.housingutilitiessystemchairman.models.user.UserResponseTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
@Mapper(componentModel = "spring", uses = { CityMapper.class, StreetMapper.class, HouseMapper.class })
public interface UserMapper {
    @Mapping(target = "id", expression = "java(user.getId() != null ? user.getId() : null)")
    @Mapping(target = "cityName", source = "city.name")
    @Mapping(target = "streetName", source = "street.name")
    @Mapping(target = "houseNumber", source = "house.houseNumber")
    @Mapping(target = "apartmentNumber", source = "apartmentNumber")
    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    UserResponseTable toResponseTable(User user);
    default Page<UserResponseTable> toResponseTablePage(Page<User> users) {
        if (users == null) {
            return null;
        }
        List<UserResponseTable> responseList = toResponseTableList(users.getContent());
        return new PageImpl<>(responseList, users.getPageable(), users.getTotalElements());
    }
    default PageResponse<UserResponseTable> toPageResponse(Page<User> users) {
        if (users == null) {
            return null;
        }
        Page<UserResponseTable> page = toResponseTablePage(users);
        return PageResponse.of(page);
    }
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "login", source = "login")
    @Mapping(target = "password", source = "password")
    User toEntity(UserRequest userRequest);
    @Mapping(target = "id", expression = "java(user.getId() != null ? user.getId() : null)")
    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "street", source = "street")
    @Mapping(target = "house", source = "house")
    @Mapping(target = "login", source = "login")
    UserResponse mapUserToResponse(User user);
    List<UserResponse> toResponseList(List<User> users);
    List<UserResponseTable> toResponseTableList(List<User> users);
    default Page<UserResponse> toResponsePage(Page<User> users) {
        if (users == null) {
            return null;
        }
        List<UserResponse> responseList = toResponseList(users.getContent());
        return new PageImpl<>(responseList, users.getPageable(), users.getTotalElements());
    }
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "street", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bills", ignore = true)
    @Mapping(target = "status", ignore = true)
    void partialUpdate(UserRequest userRequest, @MappingTarget User user);
}
