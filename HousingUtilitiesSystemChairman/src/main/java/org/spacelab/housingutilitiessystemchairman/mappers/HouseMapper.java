package org.spacelab.housingutilitiessystemchairman.mappers;

import org.mapstruct.*;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.models.location.HouseRequest;
import org.spacelab.housingutilitiessystemchairman.models.location.HouseResponse;
import org.spacelab.housingutilitiessystemchairman.models.location.HouseResponseTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
@Mapper(componentModel = "spring")
public interface HouseMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "street", ignore = true)
    @Mapping(target = "chairman", ignore = true)
    @Mapping(target = "status", ignore = true)
    House toEntity(HouseRequest houseRequest);
    @Named("mapHouseToResponse")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "number", source = "houseNumber")
    @Mapping(target = "houseNumber", source = "houseNumber")
    @Mapping(target = "status", expression = "java(house.getStatus() != null ? house.getStatus().name() : null)")
    @Mapping(target = "street", source = "street")
    @Mapping(target = "chairman", source = "chairman")
    HouseResponse mapHouseToResponse(House house);
    @Mapping(target = "id", source = "id")
    @Mapping(target = "houseNumber", source = "houseNumber")
    @Mapping(target = "streetName", source = "street.name")
    @Mapping(target = "cityName", source = "street.city.name")
    @Mapping(target = "chairmanFullName", expression = "java(house.getChairman() != null ? house.getChairman().getFullName() : null)")
    @Mapping(target = "status", expression = "java(house.getStatus() != null ? house.getStatus().name() : null)")
    HouseResponseTable toResponseTable(House house);
    @IterableMapping(qualifiedByName = "mapHouseToResponse")
    List<HouseResponse> toResponseList(List<House> houses);
    List<HouseResponseTable> toResponseTableList(List<House> houses);
    default Page<HouseResponse> toResponsePage(Page<House> houses) {
        if (houses == null) {
            return null;
        }
        List<HouseResponse> responseList = toResponseList(houses.getContent());
        return new PageImpl<>(responseList, houses.getPageable(), houses.getTotalElements());
    }
    default Page<HouseResponseTable> toResponseTablePage(Page<House> houses) {
        if (houses == null) {
            return null;
        }
        List<HouseResponseTable> responseList = toResponseTableList(houses.getContent());
        return new PageImpl<>(responseList, houses.getPageable(), houses.getTotalElements());
    }
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "street", ignore = true)
    @Mapping(target = "chairman", ignore = true)
    @Mapping(target = "status", ignore = true)
    void partialUpdate(HouseRequest houseRequest, @MappingTarget House house);
}
