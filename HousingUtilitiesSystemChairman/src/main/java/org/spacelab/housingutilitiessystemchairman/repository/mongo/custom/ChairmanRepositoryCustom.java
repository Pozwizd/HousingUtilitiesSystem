package org.spacelab.housingutilitiessystemchairman.repository.mongo.custom;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.models.filters.chairman.ChairmanRequestTable;
import org.springframework.data.domain.Page;
public interface ChairmanRepositoryCustom {
    Page<Chairman> findChairmenWithFilters(ChairmanRequestTable chairmanRequestTable);
}
