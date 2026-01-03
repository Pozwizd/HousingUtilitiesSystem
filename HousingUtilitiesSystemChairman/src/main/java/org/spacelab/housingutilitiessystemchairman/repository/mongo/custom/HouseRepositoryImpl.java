package org.spacelab.housingutilitiessystemchairman.repository.mongo.custom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.models.filters.house.HouseRequestTable;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
@Repository
@RequiredArgsConstructor
@Slf4j
public class HouseRepositoryImpl implements HouseRepositoryCustom {
    private final MongoTemplate mongoTemplate;
    @Override
    public Page<House> findHousesWithFilters(HouseRequestTable filter) {
        log.debug("Построение оптимизированной агрегации для домов с фильтрами: {}", filter);
        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.ASC, "houseNumber")
        );
        long totalCount = countHousesWithFilters(filter);
        List<House> houses = findHousesWithFiltersInternal(filter, pageable);
        log.debug("Найдено {} домов из {}", houses.size(), totalCount);
        return new PageImpl<>(houses, pageable, totalCount);
    }
    private List<House> findHousesWithFiltersInternal(HouseRequestTable filter, Pageable pageable) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (!preLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0]))));
        }
        operations.add(sort(pageable.getSort()));
        operations.add(skip((long) pageable.getPageNumber() * pageable.getPageSize()));
        operations.add(limit(pageable.getPageSize()));
        if (needsStreetLookup(filter)) {
            operations.add(lookup("street", "street", "_id", "streetData"));
            operations.add(unwind("streetData", true));
            if (needsCityLookup(filter)) {
                operations.add(lookup("city", "streetData.city", "_id", "cityData"));
                operations.add(unwind("cityData", true));
            }
        } else {
            operations.add(lookup("street", "street", "_id", "streetData"));
            operations.add(unwind("streetData", true));
            operations.add(lookup("city", "streetData.city", "_id", "cityData"));
            operations.add(unwind("cityData", true));
        }
        if (needsChairmanLookup(filter)) {
            operations.add(lookup("chairman", "chairman", "_id", "chairmanData"));
            operations.add(unwind("chairmanData", true));
        } else {
            operations.add(lookup("chairman", "chairman", "_id", "chairmanData"));
            operations.add(unwind("chairmanData", true));
        }
        List<Criteria> postLookupCriteria = buildPostLookupCriteria(filter);
        if (!postLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(postLookupCriteria.toArray(new Criteria[0]))));
        }
        Aggregation aggregation = newAggregation(operations);
        return mongoTemplate.aggregate(aggregation, "house", House.class).getMappedResults();
    }
    private long countHousesWithFilters(HouseRequestTable filter) {
        if (hasRelatedCollectionFilters(filter)) {
            return countWithAggregation(filter);
        }
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (preLookupCriteria.isEmpty()) {
            return mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), House.class);
        }
        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
        query.addCriteria(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0])));
        return mongoTemplate.count(query, House.class);
    }
    private long countWithAggregation(HouseRequestTable filter) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (!preLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0]))));
        }
        if (needsStreetLookup(filter)) {
            operations.add(lookup("street", "street", "_id", "streetData"));
            operations.add(unwind("streetData", true));
            if (needsCityLookup(filter)) {
                operations.add(lookup("city", "streetData.city", "_id", "cityData"));
                operations.add(unwind("cityData", true));
            }
        }
        if (needsChairmanLookup(filter)) {
            operations.add(lookup("chairman", "chairman", "_id", "chairmanData"));
            operations.add(unwind("chairmanData", true));
        }
        List<Criteria> postLookupCriteria = buildPostLookupCriteria(filter);
        if (!postLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(postLookupCriteria.toArray(new Criteria[0]))));
        }
        operations.add(count().as("total"));
        Aggregation aggregation = newAggregation(operations);
        org.bson.Document result = mongoTemplate.aggregate(aggregation, "house", org.bson.Document.class)
                .getUniqueMappedResult();
        return result != null ? ((Number) result.get("total")).longValue() : 0;
    }
    private List<Criteria> buildPreLookupCriteria(HouseRequestTable filter) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(filter.getHouseNumber())) {
            String houseNumberPattern = filter.getHouseNumber().trim();
            log.debug("Применяется фильтр по номеру дома: {}", houseNumberPattern);
            criteriaList.add(Criteria.where("houseNumber").regex(houseNumberPattern, "i"));
        }
        if (StringUtils.hasText(filter.getStatus())) {
            String statusPattern = filter.getStatus().trim();
            log.debug("Применяется фильтр по статусу: {}", statusPattern);
            criteriaList.add(Criteria.where("status").regex(statusPattern, "i"));
        }
        return criteriaList;
    }
    private List<Criteria> buildPostLookupCriteria(HouseRequestTable filter) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(filter.getStreetName())) {
            String streetNamePattern = filter.getStreetName().trim();
            log.debug("Применяется фильтр по улице: {}", streetNamePattern);
            criteriaList.add(Criteria.where("streetData.name").regex(streetNamePattern, "i"));
        }
        if (StringUtils.hasText(filter.getCityName())) {
            String cityNamePattern = filter.getCityName().trim();
            log.debug("Применяется фильтр по городу: {}", cityNamePattern);
            criteriaList.add(Criteria.where("cityData.name").regex(cityNamePattern, "i"));
        }
        if (StringUtils.hasText(filter.getChairmanFullName())) {
            String chairmanNamePattern = filter.getChairmanFullName().trim();
            log.debug("Применяется фильтр по председателю: {}", chairmanNamePattern);
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("chairmanData.firstName").regex(chairmanNamePattern, "i"),
                    Criteria.where("chairmanData.middleName").regex(chairmanNamePattern, "i"),
                    Criteria.where("chairmanData.lastName").regex(chairmanNamePattern, "i")
            ));
        }
        return criteriaList;
    }
    private boolean hasRelatedCollectionFilters(HouseRequestTable filter) {
        return needsStreetLookup(filter) || needsChairmanLookup(filter);
    }
    private boolean needsStreetLookup(HouseRequestTable filter) {
        return StringUtils.hasText(filter.getStreetName()) || StringUtils.hasText(filter.getCityName());
    }
    private boolean needsCityLookup(HouseRequestTable filter) {
        return StringUtils.hasText(filter.getCityName());
    }
    private boolean needsChairmanLookup(HouseRequestTable filter) {
        return StringUtils.hasText(filter.getChairmanFullName());
    }
}
