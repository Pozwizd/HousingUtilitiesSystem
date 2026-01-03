package org.spacelab.housingutilitiessystemadmin.controller.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.models.PageResponse;
import org.spacelab.housingutilitiessystemadmin.models.filters.user.UserRequestTable;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponseTable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для расширенного поиска пользователей через Elasticsearch
 * с пагинацией и фильтрацией
 */
@RestController
@RequestMapping("/api/users/search-table")
@RequiredArgsConstructor
@Slf4j
public class UserSearchElasticsearchController {

    private final org.spacelab.housingutilitiessystemadmin.service.UserSearchService userSearchService;

    /**
     * Получение таблицы пользователей с фильтрацией и пагинацией
     * POST /api/users/search-table
     * 
     * @param userRequestTable - фильтры и параметры пагинации
     * @return страница с пользователями UserResponseTable
     */
    @PostMapping
    public ResponseEntity<PageResponse<UserResponseTable>> getUsersTable(
            @Valid @RequestBody UserRequestTable userRequestTable) {
        log.info("Запрос таблицы пользователей с фильтрами: {}", userRequestTable);
        PageResponse<UserResponseTable> result = userSearchService.getUsersTable(userRequestTable);
        log.info("Возвращено {} пользователей из {}", 
                result.getNumberOfElements(), 
                result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    /**
     * Индексация всех пользователей из MongoDB в Elasticsearch
     * POST /api/users/search-table/index-all
     * 
     * @return сообщение об успешной индексации
     */
    @PostMapping("/index-all")
    public ResponseEntity<String> indexAllUsers() {
        log.info("Запрос на индексацию всех пользователей в Elasticsearch");
        userSearchService.indexAllUsers();
        return ResponseEntity.ok("Пользователи успешно проиндексированы в Elasticsearch");
    }
}
