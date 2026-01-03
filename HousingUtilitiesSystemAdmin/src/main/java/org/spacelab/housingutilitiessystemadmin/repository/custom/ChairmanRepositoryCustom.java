package org.spacelab.housingutilitiessystemadmin.repository.custom;

import org.spacelab.housingutilitiessystemadmin.entity.Chairman;
import org.spacelab.housingutilitiessystemadmin.models.filters.chairman.ChairmanRequestTable;
import org.springframework.data.domain.Page;

public interface ChairmanRepositoryCustom {
    Page<Chairman> findChairmenWithFilters(ChairmanRequestTable chairmanRequestTable);
}

