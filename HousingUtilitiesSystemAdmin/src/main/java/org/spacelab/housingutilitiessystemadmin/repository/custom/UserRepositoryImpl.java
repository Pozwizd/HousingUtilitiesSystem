package org.spacelab.housingutilitiessystemadmin.repository.custom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.models.filters.user.UserRequestTable;
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

/**
 * Оптимизированная реализация репозитория для работы с User.
 *
 * ВАЖНО: Создать следующие индексы для оптимальной производительности:
 *
 * // Составной индекс для сортировки
 * db.user.createIndex({ "lastName": 1, "firstName": 1 })
 *
 * // Индексы для полей поиска
 * db.user.createIndex({ "phone": 1 })
 * db.user.createIndex({ "accountNumber": 1 })
 * db.user.createIndex({ "houseNumber": 1 })
 * db.user.createIndex({ "apartmentNumber": 1 })
 * db.user.createIndex({ "status": 1 })
 * db.user.createIndex({ "city": 1 })
 * db.user.createIndex({ "street": 1 })
 *
 * // Опционально: Text index для полнотекстового поиска по ФИО
 * db.user.createIndex({
 *     "firstName": "text",
 *     "middleName": "text",
 *     "lastName": "text"
 * }, {
 *     name: "user_fullname_text",
 *     default_language: "russian",
 *     weights: { firstName: 10, lastName: 10, middleName: 5 }
 * })
 */
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

        // Получаем общее количество отдельным запросом (быстрее, чем $facet)
        long totalCount = countUsersWithFilters(filter);

        // Основной запрос с пагинацией
        List<User> users = findUsersWithFiltersInternal(filter, pageable);

        log.debug("Найдено {} пользователей из {}", users.size(), totalCount);

        return new PageImpl<>(users, pageable, totalCount);
    }

    /**
     * Основной метод получения данных с оптимизированным pipeline
     */
    private List<User> findUsersWithFiltersInternal(UserRequestTable filter, Pageable pageable) {
        List<AggregationOperation> operations = new ArrayList<>();

        // ШАГ 1: $match на основной коллекции ДО $lookup (критично для производительности!)
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (!preLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0]))));
        }

        // ШАГ 2: $sort ДО $skip и $limit
        operations.add(sort(pageable.getSort()));

        // ШАГ 3: $skip и $limit ДО $lookup (уменьшает количество JOIN'ов!)
        operations.add(skip((long) pageable.getPageNumber() * pageable.getPageSize()));
        operations.add(limit(pageable.getPageSize()));

        // ШАГ 4: $lookup только для отфильтрованных и ограниченных документов
        if (needsCityLookup(filter)) {
            operations.add(lookup("city", "city", "_id", "cityData"));
            operations.add(unwind("cityData", true));
        }

        if (needsStreetLookup(filter)) {
            operations.add(lookup("street", "street", "_id", "streetData"));
            operations.add(unwind("streetData", true));
        }

        // ШАГ 5: $match для фильтров по связанным коллекциям (после $lookup)
        List<Criteria> postLookupCriteria = buildPostLookupCriteria(filter);
        if (!postLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(postLookupCriteria.toArray(new Criteria[0]))));
        }

        Aggregation aggregation = newAggregation(operations);

        return mongoTemplate.aggregate(aggregation, "user", User.class).getMappedResults();
    }

    /**
     * Отдельный count запрос без $lookup для производительности
     */
    private long countUsersWithFilters(UserRequestTable filter) {
        // Если есть фильтры по связанным коллекциям, нужна агрегация с $lookup
        if (hasRelatedCollectionFilters(filter)) {
            return countWithAggregation(filter);
        }

        // Иначе используем простой count (намного быстрее)
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (preLookupCriteria.isEmpty()) {
            return mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), User.class);
        }

        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
        query.addCriteria(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0])));
        return mongoTemplate.count(query, User.class);
    }

    /**
     * Count с агрегацией для фильтров по связанным коллекциям
     */
    private long countWithAggregation(UserRequestTable filter) {
        List<AggregationOperation> operations = new ArrayList<>();

        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (!preLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0]))));
        }

        // Добавляем только необходимые $lookup для фильтров
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

    /**
     * Критерии, которые можно применить ДО $lookup (на основной коллекции)
     */
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

    /**
     * Критерии для связанных коллекций (применяются ПОСЛЕ $lookup)
     */
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

    /**
     * Проверка наличия фильтров по связанным коллекциям
     */
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
