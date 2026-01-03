package org.spacelab.housingutilitiessystemchairman.repository.mongo.custom;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.models.filters.house.HouseRequestTable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
@Repository
public interface HouseRepositoryCustom {
    Page<House> findHousesWithFilters(HouseRequestTable filter);
}
