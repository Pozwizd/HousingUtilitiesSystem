package org.spacelab.housingutilitiessystemadmin.mappers;

import org.mapstruct.*;
import org.spacelab.housingutilitiessystemadmin.entity.Chairman;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanRequest;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponse;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponseTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChairmanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "online", ignore = true)
    @Mapping(target = "lastActiveAt", ignore = true)
    @Mapping(target = "conversations", ignore = true)
    @Mapping(target = "house", ignore = true)
    Chairman toEntity(ChairmanRequest chairmanRequest);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", expression = "java(chairman.getFullName())")
    ChairmanResponse mapChairmanToResponse(Chairman chairman);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", expression = "java(chairman.getFullName())")
    ChairmanResponseTable toResponseTable(Chairman chairman);

    List<ChairmanResponse> toResponseList(List<Chairman> chairmen);

    List<ChairmanResponseTable> toResponseTableList(List<Chairman> chairmen);

    default Page<ChairmanResponse> toResponsePage(Page<Chairman> chairmen) {
        if (chairmen == null) {
            return null;
        }
        List<ChairmanResponse> responseList = toResponseList(chairmen.getContent());
        return new PageImpl<>(responseList, chairmen.getPageable(), chairmen.getTotalElements());
    }

    default Page<ChairmanResponseTable> toResponseTablePage(Page<Chairman> chairmen) {
        if (chairmen == null) {
            return null;
        }
        List<ChairmanResponseTable> responseList = toResponseTableList(chairmen.getContent());
        return new PageImpl<>(responseList, chairmen.getPageable(), chairmen.getTotalElements());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "online", ignore = true)
    @Mapping(target = "lastActiveAt", ignore = true)
    @Mapping(target = "conversations", ignore = true)
    @Mapping(target = "house", ignore = true)
    void partialUpdate(ChairmanRequest chairmanRequest, @MappingTarget Chairman chairman);
}

