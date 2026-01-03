package org.spacelab.housingutilitiessystemchairman.repository.mongo.custom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.models.filters.user.UserRequestTable;
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
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final MongoTemplate mongoTemplate;
    @Override
    public Page<User> findUsersWithFilters(UserRequestTable filter) {
        log.debug("Построение оптимизированной агрегации для пользователей с фильтрами: {}", filter);
        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.ASC, "lastName", "firstName")
        );
        long totalCount = countUsersWithFilters(filter);
        List<User> users = findUsersWithFiltersInternal(filter, pageable);
        log.debug("Найдено {} пользователей из {}", users.size(), totalCount);
        return new PageImpl<>(users, pageable, totalCount);
    }
    private List<User> findUsersWithFiltersInternal(UserRequestTable filter, Pageable pageable) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (!preLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0]))));
        }
        operations.add(sort(pageable.getSort()));
        operations.add(skip((long) pageable.getPageNumber() * pageable.getPageSize()));
        operations.add(limit(pageable.getPageSize()));
        if (needsCityLookup(filter)) {
            operations.add(lookup("city", "city", "_id", "cityData"));
            operations.add(unwind("cityData", true));
        }
        if (needsStreetLookup(filter)) {
            operations.add(lookup("street", "street", "_id", "streetData"));
            operations.add(unwind("streetData", true));
        }
        List<Criteria> postLookupCriteria = buildPostLookupCriteria(filter);
        if (!postLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(postLookupCriteria.toArray(new Criteria[0]))));
        }
        Aggregation aggregation = newAggregation(operations);
        return mongoTemplate.aggregate(aggregation, "user", User.class).getMappedResults();
    }
    private long countUsersWithFilters(UserRequestTable filter) {
        if (hasRelatedCollectionFilters(filter)) {
            return countWithAggregation(filter);
        }
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (preLookupCriteria.isEmpty()) {
            return mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), User.class);
        }
        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
        query.addCriteria(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0])));
        return mongoTemplate.count(query, User.class);
    }
    private long countWithAggregation(UserRequestTable filter) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (!preLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0]))));
        }
        if (needsCityLookup(filter)) {
            operations.add(lookup("city", "city", "_id", "cityData"));
            operations.add(unwind("cityData", true));
        }
        if (needsStreetLookup(filter)) {
            operations.add(lookup("street", "street", "_id", "streetData"));
            operations.add(unwind("streetData", true));
        }
        List<Criteria> postLookupCriteria = buildPostLookupCriteria(filter);
        if (!postLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(postLookupCriteria.toArray(new Criteria[0]))));
        }
        operations.add(count().as("total"));
        Aggregation aggregation = newAggregation(operations);
        org.bson.Document result = mongoTemplate.aggregate(aggregation, "user", org.bson.Document.class)
                .getUniqueMappedResult();
        return result != null ? ((Number) result.get("total")).longValue() : 0;
    }
    private List<Criteria> buildPreLookupCriteria(UserRequestTable filter) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(filter.getFullName())) {
            String namePattern = filter.getFullName().trim();
            log.debug("Применяется фильтр по ФИО: {}", namePattern);
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("firstName").regex(namePattern, "i"),
                    Criteria.where("middleName").regex(namePattern, "i"),
                    Criteria.where("lastName").regex(namePattern, "i")
            ));
        }
        if (StringUtils.hasText(filter.getPhoneNumber())) {
            String phonePattern = filter.getPhoneNumber().trim();
            log.debug("Применяется фильтр по телефону: {}", phonePattern);
            criteriaList.add(Criteria.where("phone").regex(phonePattern, "i"));
        }
        if (StringUtils.hasText(filter.getHouseNumber())) {
            log.debug("Применяется фильтр по номеру дома: {}", filter.getHouseNumber());
            criteriaList.add(Criteria.where("houseNumber").is(filter.getHouseNumber()));
        }
        if (StringUtils.hasText(filter.getApartmentNumber())) {
            log.debug("Применяется фильтр по номеру квартиры: {}", filter.getApartmentNumber());
            criteriaList.add(Criteria.where("apartmentNumber").is(filter.getApartmentNumber()));
        }
        if (StringUtils.hasText(filter.getAccountNumber())) {
            String accountPattern = filter.getAccountNumber().trim();
            log.debug("Применяется фильтр по лицевому счету: {}", accountPattern);
            criteriaList.add(Criteria.where("accountNumber").regex(accountPattern, "i"));
        }
        if (filter.getStatus() != null) {
            log.debug("Применяется фильтр по статусу: {}", filter.getStatus());
            criteriaList.add(Criteria.where("status").is(filter.getStatus().toString()));
        }
        return criteriaList;
    }
    private List<Criteria> buildPostLookupCriteria(UserRequestTable filter) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(filter.getCityName())) {
            String cityNamePattern = filter.getCityName().trim();
            log.debug("Применяется фильтр по городу: {}", cityNamePattern);
            criteriaList.add(Criteria.where("cityData.name").regex(cityNamePattern, "i"));
        }
        if (StringUtils.hasText(filter.getStreetName())) {
            String streetNamePattern = filter.getStreetName().trim();
            log.debug("Применяется фильтр по улице: {}", streetNamePattern);
            criteriaList.add(Criteria.where("streetData.name").regex(streetNamePattern, "i"));
        }
        return criteriaList;
    }
    private boolean hasRelatedCollectionFilters(UserRequestTable filter) {
        return needsCityLookup(filter) || needsStreetLookup(filter);
    }
    private boolean needsCityLookup(UserRequestTable filter) {
        return StringUtils.hasText(filter.getCityName());
    }
    private boolean needsStreetLookup(UserRequestTable filter) {
        return StringUtils.hasText(filter.getStreetName());
    }
}
