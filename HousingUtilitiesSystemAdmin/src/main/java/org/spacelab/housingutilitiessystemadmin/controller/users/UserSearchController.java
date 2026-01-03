package org.spacelab.housingutilitiessystemadmin.controller.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.search.UserSearchDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search/users")
@RequiredArgsConstructor
@Slf4j
public class UserSearchController {
    
    private final org.spacelab.housingutilitiessystemadmin.search.UserSearchService userSearchService;


    
    /**
     * Индексирует всех пользователей в Elasticsearch
     * POST /api/search/users/index-all
     */
    @PostMapping("/index-all")
    public ResponseEntity<String> indexAllUsers() {
        log.info("Indexing all users request received");
        userSearchService.indexAllUsers();
        return ResponseEntity.ok("All users indexed successfully");
    }
    
    /**
     * Поиск по имени, фамилии или отчеству
     * GET /api/search/users/by-name?name=Иван
     */
    @GetMapping("/by-name")
    public ResponseEntity<List<UserSearchDocument>> searchByName(@RequestParam String name) {
        log.info("Searching users by name: {}", name);
        List<UserSearchDocument> results = userSearchService.searchByName(name);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Поиск по email
     * GET /api/search/users/by-email?email=user@example.com
     */
    @GetMapping("/by-email")
    public ResponseEntity<List<UserSearchDocument>> searchByEmail(@RequestParam String email) {
        log.info("Searching users by email: {}", email);
        List<UserSearchDocument> results = userSearchService.searchByEmail(email);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Поиск по телефону
     * GET /api/search/users/by-phone?phone=+1234567890
     */
    @GetMapping("/by-phone")
    public ResponseEntity<List<UserSearchDocument>> searchByPhone(@RequestParam String phone) {
        log.info("Searching users by phone: {}", phone);
        List<UserSearchDocument> results = userSearchService.searchByPhone(phone);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Поиск по номеру счета
     * GET /api/search/users/by-account?accountNumber=ACC123456
     */
    @GetMapping("/by-account")
    public ResponseEntity<List<UserSearchDocument>> searchByAccountNumber(@RequestParam String accountNumber) {
        log.info("Searching users by account number: {}", accountNumber);
        List<UserSearchDocument> results = userSearchService.searchByAccountNumber(accountNumber);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Поиск по городу
     * GET /api/search/users/by-city?city=Москва
     */
    @GetMapping("/by-city")
    public ResponseEntity<List<UserSearchDocument>> searchByCity(@RequestParam String city) {
        log.info("Searching users by city: {}", city);
        List<UserSearchDocument> results = userSearchService.searchByCity(city);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Поиск по статусу
     * GET /api/search/users/by-status?status=ACTIVE
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<UserSearchDocument>> searchByStatus(@RequestParam String status) {
        log.info("Searching users by status: {}", status);
        List<UserSearchDocument> results = userSearchService.searchByStatus(status);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Комплексный поиск по всем полям
     * GET /api/search/users?query=Иван
     */
    @GetMapping
    public ResponseEntity<List<UserSearchDocument>> complexSearch(@RequestParam String query) {
        log.info("Complex search with query: {}", query);
        List<UserSearchDocument> results = userSearchService.complexSearch(query);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Удаляет пользователя из индекса
     * DELETE /api/search/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteFromIndex(@PathVariable String userId) {
        log.info("Deleting user from index: {}", userId);
        userSearchService.deleteFromIndex(userId);
        return ResponseEntity.ok("User deleted from index successfully");
    }
}
