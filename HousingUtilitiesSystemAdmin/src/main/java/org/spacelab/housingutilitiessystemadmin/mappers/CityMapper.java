package org.spacelab.housingutilitiessystemadmin.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.spacelab.housingutilitiessystemadmin.models.location.CityResponse;

import java.util.List;


@Mapper(componentModel = "spring")
public interface CityMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CityResponse toResponse(City city);

    List<CityResponse> toResponse(List<City> cities);
}

