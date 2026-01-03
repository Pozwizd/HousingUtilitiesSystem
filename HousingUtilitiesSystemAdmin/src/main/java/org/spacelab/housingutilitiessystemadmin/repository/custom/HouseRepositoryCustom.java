package org.spacelab.housingutilitiessystemadmin.repository.custom;

import org.spacelab.housingutilitiessystemadmin.entity.location.House;
import org.spacelab.housingutilitiessystemadmin.models.filters.house.HouseRequestTable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseRepositoryCustom {
    Page<House> findHousesWithFilters(HouseRequestTable filter);
}

