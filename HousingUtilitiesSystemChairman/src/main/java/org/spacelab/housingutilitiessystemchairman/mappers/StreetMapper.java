package org.spacelab.housingutilitiessystemchairman.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.spacelab.housingutilitiessystemchairman.entity.location.Street;
import org.spacelab.housingutilitiessystemchairman.models.location.StreetResponse;

import java.util.List;
@Mapper(componentModel = "spring")
public interface StreetMapper {
    @Mapping(target = "id", expression = "java(street.getId() != null ? street.getId().toString() : null)")
    StreetResponse toResponse(Street street);
    List<StreetResponse> toResponse(List<Street> streets);
}
