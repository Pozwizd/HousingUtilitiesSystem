package org.spacelab.housingutilitiessystemchairman.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.spacelab.housingutilitiessystemchairman.entity.location.City;
import org.spacelab.housingutilitiessystemchairman.models.location.CityResponse;

import java.util.List;
@Mapper(componentModel = "spring")
public interface CityMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CityResponse toResponse(City city);
    List<CityResponse> toResponse(List<City> cities);
}
