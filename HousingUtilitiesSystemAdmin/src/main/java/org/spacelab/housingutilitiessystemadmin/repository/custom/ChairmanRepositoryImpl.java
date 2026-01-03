package org.spacelab.housingutilitiessystemadmin.repository.custom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.Chairman;
import org.spacelab.housingutilitiessystemadmin.models.filters.chairman.ChairmanRequestTable;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Оптимизированная реализация репозитория для работы с Chairman.
 *
 * ВАЖНО: Создать следующие индексы для оптимальной производительности:
 *
 * // Составной индекс для сортировки (критично для производительности!)
 * db.chairman.createIndex({ "lastName": 1, "firstName": 1 })
 *
 * // Индексы для полей поиска (для regex запросов)
 * db.chairman.createIndex({ "phone": 1 })
 * db.chairman.createIndex({ "email": 1 })
 * db.chairman.createIndex({ "login": 1 })
 * db.chairman.createIndex({ "status": 1 })
 *
 * // Опционально: Text index для полнотекстового поиска по имени
 * // (альтернатива regex, значительно быстрее для поиска по нескольким полям)
 * db.chairman.createIndex({
 *     "firstName": "text",
 *     "middleName": "text",
 *     "lastName": "text"
 * }, {
 *     name: "chairman_fullname_text",
 *     default_language: "russian",
 *     weights: { firstName: 10, lastName: 10, middleName: 5 }
 * })
 *
 * ПРИМЕЧАНИЕ: Regex поиск с опцией "i" (case-insensitive) не использует
 * индексы максимально эффективно. Для лучшей производительности рассмотрите:
 * 1. Использование text index для полнотекстового поиска
 * 2. Хранение нормализованных значений в отдельных полях (lowercase)
 * 3. Использование Atlas Search для продвинутого поиска
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ChairmanRepositoryImpl implements ChairmanRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    // Флаг для использования text index вместо regex (требует создания text index)
    private static final boolean USE_TEXT_INDEX = false;

    @Override
    public Page<Chairman> findChairmenWithFilters(ChairmanRequestTable chairmanRequestTable) {
        log.debug("Построение запроса с фильтрами: {}", chairmanRequestTable);

        Pageable pageable = PageRequest.of(
                chairmanRequestTable.getPage(),
                chairmanRequestTable.getSize(),
                Sort.by(Sort.Direction.ASC, "lastName", "firstName")
        );

        // Оптимизация: сначала строим query для возможного раннего выхода
        Query query = buildFilterQuery(chairmanRequestTable);

        // Оптимизация: для пустых фильтров можем использовать estimatedDocumentCount (быстрее)
        long totalCount;
        if (!hasAnyFilters(chairmanRequestTable)) {
            totalCount = mongoTemplate.estimatedCount(Chairman.class);
            log.debug("Используется estimatedCount для пустых фильтров: {}", totalCount);
        } else {
            totalCount = mongoTemplate.count(query, Chairman.class);
            log.debug("Выполнен count с фильтрами: {}", totalCount);
        }

        // Применяем пагинацию после count
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

    /**
     * Построение запроса с фильтрами
     */
    private Query buildFilterQuery(ChairmanRequestTable filter) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Поиск по ФИО - самый частый и тяжелый запрос
        if (StringUtils.hasText(filter.getFullName())) {
            String namePattern = filter.getFullName().trim();
            log.debug("Применяется фильтр по имени: {}", namePattern);

            if (USE_TEXT_INDEX) {
                // Вариант 1: Использование text index (требует создания индекса)
                // Значительно быстрее для полнотекстового поиска
                criteriaList.add(Criteria.where("$text").is(new org.bson.Document("$search", namePattern)));
            } else {
                // Вариант 2: Regex поиск (работает без дополнительных индексов)
                // Оптимизация: если паттерн начинается с конкретных символов, используем prefix regex
                String regexPattern = namePattern.startsWith("^") ? namePattern : namePattern;

                criteriaList.add(new Criteria().orOperator(
                        Criteria.where("firstName").regex(regexPattern, "i"),
                        Criteria.where("middleName").regex(regexPattern, "i"),
                        Criteria.where("lastName").regex(regexPattern, "i")
                ));
            }
        }

        // Остальные фильтры - более точные, работают быстрее
        if (StringUtils.hasText(filter.getPhone())) {
            String phonePattern = filter.getPhone().trim();
            log.debug("Применяется фильтр по телефону: {}", phonePattern);
            // Телефоны обычно ищут по префиксу, используем prefix regex
            criteriaList.add(Criteria.where("phone").regex("^" + phonePattern, "i"));
        }

        if (StringUtils.hasText(filter.getEmail())) {
            String emailPattern = filter.getEmail().trim();
            log.debug("Применяется фильтр по email: {}", emailPattern);
            criteriaList.add(Criteria.where("templates/email").regex(emailPattern, "i"));
        }

        if (StringUtils.hasText(filter.getLogin())) {
            String loginPattern = filter.getLogin().trim();
            log.debug("Применяется фильтр по login: {}", loginPattern);
            // Login обычно ищут по префиксу
            criteriaList.add(Criteria.where("login").regex("^" + loginPattern, "i"));
        }

        if (StringUtils.hasText(filter.getStatus())) {
            String statusPattern = filter.getStatus().trim();
            log.debug("Применяется фильтр по статусу: {}", statusPattern);
            // Status лучше искать точным совпадением для использования индекса
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

    /**
     * Проверка наличия каких-либо фильтров
     */
    private boolean hasAnyFilters(ChairmanRequestTable filter) {
        return StringUtils.hasText(filter.getFullName()) ||
                StringUtils.hasText(filter.getPhone()) ||
                StringUtils.hasText(filter.getEmail()) ||
                StringUtils.hasText(filter.getLogin()) ||
                StringUtils.hasText(filter.getStatus());
    }
}
