package org.spacelab.housingutilitiessystemchairman.repository.mongo.custom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.models.filters.chairman.ChairmanRequestTable;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
@Repository
@RequiredArgsConstructor
@Slf4j
public class ChairmanRepositoryImpl implements ChairmanRepositoryCustom {
    private final MongoTemplate mongoTemplate;
    private static final boolean USE_TEXT_INDEX = false;
    @Override
    public Page<Chairman> findChairmenWithFilters(ChairmanRequestTable chairmanRequestTable) {
        log.debug("Построение запроса с фильтрами: {}", chairmanRequestTable);
        Pageable pageable = PageRequest.of(
                chairmanRequestTable.getPage(),
                chairmanRequestTable.getSize(),
                Sort.by(Sort.Direction.ASC, "lastName", "firstName")
        );
        Query query = buildFilterQuery(chairmanRequestTable);
        long totalCount;
        if (!hasAnyFilters(chairmanRequestTable)) {
            totalCount = mongoTemplate.estimatedCount(Chairman.class);
            log.debug("Используется estimatedCount для пустых фильтров: {}", totalCount);
        } else {
            totalCount = mongoTemplate.count(query, Chairman.class);
            log.debug("Выполнен count с фильтрами: {}", totalCount);
        }
        query.with(pageable);
        List<Chairman> chairmen = mongoTemplate.find(query, Chairman.class);
        log.debug("Найдено {} председателей из {} (страница {} из {})",
                chairmen.size(),
                totalCount,
                chairmanRequestTable.getPage() + 1,
                (totalCount + chairmanRequestTable.getSize() - 1) / chairmanRequestTable.getSize()
        );
        return new PageImpl<>(chairmen, pageable, totalCount);
    }
    private Query buildFilterQuery(ChairmanRequestTable filter) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(filter.getFullName())) {
            String namePattern = filter.getFullName().trim();
            log.debug("Применяется фильтр по имени: {}", namePattern);
            if (USE_TEXT_INDEX) {
                criteriaList.add(Criteria.where("$text").is(new org.bson.Document("$search", namePattern)));
            } else {
                String regexPattern = namePattern.startsWith("^") ? namePattern : namePattern;
                criteriaList.add(new Criteria().orOperator(
                        Criteria.where("firstName").regex(regexPattern, "i"),
                        Criteria.where("middleName").regex(regexPattern, "i"),
                        Criteria.where("lastName").regex(regexPattern, "i")
                ));
            }
        }
        if (StringUtils.hasText(filter.getPhone())) {
            String phonePattern = filter.getPhone().trim();
            log.debug("Применяется фильтр по телефону: {}", phonePattern);
            criteriaList.add(Criteria.where("phone").regex("^" + phonePattern, "i"));
        }
        if (StringUtils.hasText(filter.getEmail())) {
            String emailPattern = filter.getEmail().trim();
            log.debug("Применяется фильтр по email: {}", emailPattern);
            criteriaList.add(Criteria.where("email").regex(emailPattern, "i"));
        }
        if (StringUtils.hasText(filter.getLogin())) {
            String loginPattern = filter.getLogin().trim();
            log.debug("Применяется фильтр по login: {}", loginPattern);
            criteriaList.add(Criteria.where("login").regex("^" + loginPattern, "i"));
        }
        if (StringUtils.hasText(filter.getStatus())) {
            String statusPattern = filter.getStatus().trim();
            log.debug("Применяется фильтр по статусу: {}", statusPattern);
            criteriaList.add(Criteria.where("status").is(statusPattern.toUpperCase()));
        }
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(
                    criteriaList.toArray(new Criteria[0])
            ));
            log.debug("Создан запрос с {} критериями", criteriaList.size());
        } else {
            log.debug("Фильтры не применены, возвращаются все записи");
        }
        return query;
    }
    private boolean hasAnyFilters(ChairmanRequestTable filter) {
        return StringUtils.hasText(filter.getFullName()) ||
                StringUtils.hasText(filter.getPhone()) ||
                StringUtils.hasText(filter.getEmail()) ||
                StringUtils.hasText(filter.getLogin()) ||
                StringUtils.hasText(filter.getStatus());
    }
}
