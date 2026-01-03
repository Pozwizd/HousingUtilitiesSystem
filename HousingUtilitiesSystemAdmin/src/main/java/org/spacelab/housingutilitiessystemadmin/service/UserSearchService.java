package org.spacelab.housingutilitiessystemadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.models.PageResponse;
import org.spacelab.housingutilitiessystemadmin.models.filters.user.UserRequestTable;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponseTable;
import org.spacelab.housingutilitiessystemadmin.repository.UserRepository;
import org.spacelab.housingutilitiessystemadmin.search.UserSearchDocument;
import org.spacelab.housingutilitiessystemadmin.search.UserSearchRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service("advancedUserSearchService")
@RequiredArgsConstructor
@Slf4j
public class UserSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final UserSearchRepository userSearchRepository;
    private final UserRepository userRepository;

    /**
     * Получение пользователей с фильтрацией и пагинацией
     */
    public PageResponse<UserResponseTable> getUsersTable(UserRequestTable userRequestTable) {
        Page<UserSearchDocument> users = searchUsers(userRequestTable);
        Page<UserResponseTable> userResponses = mapToResponseTable(users);

        log.info("Найдено {} пользователей из {}. Страница: {}, размер: {}",
                users.getNumberOfElements(),
                users.getTotalElements(),
                userRequestTable.getPage(),
                userRequestTable.getSize());

        return PageResponse.of(userResponses);
    }

    /**
     * Индексирует всех пользователей из MongoDB в Elasticsearch
     */
    public void indexAllUsers() {
        log.info("Начало индексации пользователей в Elasticsearch...");
        
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(UserSearchDocument.class);

            if (indexOps.exists()) {
                log.info("Удаляем существующий индекс Elasticsearch 'users' для обновления маппинга");
                indexOps.delete();
            }

            indexOps.create();
            indexOps.putMapping();
            log.info("Создан индекс Elasticsearch 'users' с актуальным маппингом");

            // Получаем всех пользователей из MongoDB
            List<User> users = userRepository.findAll();
            log.info("Получено {} пользователей из MongoDB", users.size());

            // Конвертируем и сохраняем в Elasticsearch
            List<UserSearchDocument> documents = users.stream()
                    .map(this::convertToSearchDocument)
                    .collect(Collectors.toList());

            userSearchRepository.saveAll(documents);
            log.info("Успешно проиндексировано {} пользователей в Elasticsearch", documents.size());
        } catch (Exception e) {
            log.error("Ошибка при индексации пользователей: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось проиндексировать пользователей", e);
        }
    }

    /**
     * Основной метод поиска с фильтрами
     */
    private Page<UserSearchDocument> searchUsers(UserRequestTable filter) {
        // Создаем пагинацию
        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.ASC, "lastName.keyword", "firstName.keyword")
        );

        try {
            // Проверяем существование индекса
            if (!elasticsearchOperations.indexOps(UserSearchDocument.class).exists()) {
                log.warn("Индекс Elasticsearch 'users' не существует. Возвращаем пустую страницу.");
                return new PageImpl<>(List.of(), pageable, 0);
            }

            // Создаем критерии для фильтрации
            Criteria criteria = buildCriteria(filter);

            // Создаем запрос
            Query query = new CriteriaQuery(criteria);
            query.setPageable(pageable);

            // Выполняем поиск
            SearchHits<UserSearchDocument> searchHits = elasticsearchOperations.search(
                    query,
                    UserSearchDocument.class
            );

            // Преобразуем результаты
            List<UserSearchDocument> users = searchHits.getSearchHits()
                    .stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            return new PageImpl<>(users, pageable, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("Ошибка при поиске в Elasticsearch: {}. Возвращаем пустую страницу.", e.getMessage());
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    /**
     * Построение критериев фильтрации
     */
    private Criteria buildCriteria(UserRequestTable filter) {
        Criteria criteria = new Criteria();

        // Полнотекстовый поиск по ФИО
        if (StringUtils.hasText(filter.getFullName())) {
            String searchTerm = filter.getFullName().trim();
            log.debug("Поиск по ФИО: {}", searchTerm);

            // Поиск по нескольким полям
            criteria = new Criteria().or(new Criteria("firstName").matches(searchTerm))
                    .or(new Criteria("lastName").matches(searchTerm))
                    .or(new Criteria("middleName").matches(searchTerm))
                    .or(new Criteria("fullName").matches(searchTerm));
        }

        // Точные фильтры по keyword полям
        if (StringUtils.hasText(filter.getPhoneNumber())) {
            log.debug("Фильтр по телефону: {}", filter.getPhoneNumber());
            criteria = criteria.and(new Criteria("phone").contains(filter.getPhoneNumber()));
        }

        if (StringUtils.hasText(filter.getAccountNumber())) {
            log.debug("Фильтр по лицевому счету: {}", filter.getAccountNumber());
            criteria = criteria.and(new Criteria("accountNumber").contains(filter.getAccountNumber()));
        }

        if (StringUtils.hasText(filter.getCityName())) {
            log.debug("Фильтр по городу: {}", filter.getCityName());
            criteria = criteria.and(new Criteria("cityName").is(filter.getCityName()));
        }

        if (StringUtils.hasText(filter.getStreetName())) {
            log.debug("Фильтр по улице: {}", filter.getStreetName());
            criteria = criteria.and(new Criteria("streetName").is(filter.getStreetName()));
        }

        if (StringUtils.hasText(filter.getHouseNumber())) {
            log.debug("Фильтр по дому: {}", filter.getHouseNumber());
            criteria = criteria.and(new Criteria("houseNumber").is(filter.getHouseNumber()));
        }

        if (StringUtils.hasText(filter.getApartmentNumber())) {
            log.debug("Фильтр по квартире: {}", filter.getApartmentNumber());
            criteria = criteria.and(new Criteria("apartmentNumber").is(filter.getApartmentNumber()));
        }

        if (filter.getStatus() != null) {
            log.debug("Фильтр по статусу: {}", filter.getStatus());
            criteria = criteria.and(new Criteria("status").is(filter.getStatus().toString()));
        }

        return criteria;
    }

    /**
     * Маппинг в DTO для таблицы
     */
    private Page<UserResponseTable> mapToResponseTable(Page<UserSearchDocument> users) {
        List<UserResponseTable> responseList = users.getContent()
                .stream()
                .map(this::toResponseTable)
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, users.getPageable(), users.getTotalElements());
    }

    /**
     * Преобразование документа в DTO
     */
    private UserResponseTable toResponseTable(UserSearchDocument doc) {
        UserResponseTable response = new UserResponseTable();
        response.setId(doc.getId());
        response.setFullName(buildFullName(doc));
        response.setCityName(doc.getCityName());
        response.setStreetName(doc.getStreetName());
        response.setHouseNumber(doc.getHouseNumber());
        response.setApartmentNumber(doc.getApartmentNumber());
        response.setAccountNumber(doc.getAccountNumber());
        response.setPhoneNumber(doc.getPhone());
        response.setStatus(doc.getStatus());
        return response;
    }

    /**
     * Формирование полного имени
     */
    private String buildFullName(UserSearchDocument doc) {
        StringBuilder fullName = new StringBuilder();

        if (StringUtils.hasText(doc.getLastName())) {
            fullName.append(doc.getLastName());
        }
        if (StringUtils.hasText(doc.getFirstName())) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(doc.getFirstName());
        }
        if (StringUtils.hasText(doc.getMiddleName())) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(doc.getMiddleName());
        }

        return fullName.toString();
    }

    /**
     * Конвертирует User в UserSearchDocument
     */
    private UserSearchDocument convertToSearchDocument(User user) {
        return UserSearchDocument.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .cityName(user.getCity() != null ? user.getCity().getName() : null)
                .streetName(user.getStreet() != null ? user.getStreet().getName() : null)
                .houseNumber(user.getHouseNumber())
                .apartmentNumber(user.getApartmentNumber())
                .accountNumber(user.getAccountNumber())
                .status(user.getStatus() != null ? user.getStatus().toString() : null)
                .role(user.getRole() != null ? user.getRole().toString() : null)
                .build();
    }
}
