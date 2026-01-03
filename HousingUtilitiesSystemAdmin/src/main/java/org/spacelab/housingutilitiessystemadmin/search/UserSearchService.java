package org.spacelab.housingutilitiessystemadmin.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.repository.UserRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("basicUserSearchService")
@RequiredArgsConstructor
@Slf4j
public class UserSearchService {
    
    private final UserSearchRepository userSearchRepository;
    private final UserRepository userRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    
    /**
     * Индексирует пользователя в Elasticsearch
     */
    public UserSearchDocument indexUser(User user) {
        UserSearchDocument document = convertToSearchDocument(user);
        UserSearchDocument saved = userSearchRepository.save(document);
        log.info("Indexed user with id: {}", user.getId());
        return saved;
    }
    
    /**
     * Индексирует всех пользователей из MongoDB в Elasticsearch
     */
    public void indexAllUsers() {
        log.info("Starting indexing all users...");
        List<User> users = userRepository.findAll();
        List<UserSearchDocument> documents = users.stream()
                .map(this::convertToSearchDocument)
                .collect(Collectors.toList());
        
        userSearchRepository.saveAll(documents);
        log.info("Indexed {} users", documents.size());
    }
    
    /**
     * Удаляет пользователя из индекса
     */
    public void deleteFromIndex(String userId) {
        userSearchRepository.deleteById(userId);
        log.info("Deleted user from index: {}", userId);
    }
    
    /**
     * Поиск по имени, фамилии или отчеству
     */
    public List<UserSearchDocument> searchByName(String name) {
        return userSearchRepository.findByFirstNameContainingOrLastNameContainingOrMiddleNameContaining(
                name, name, name);
    }
    
    /**
     * Поиск по email
     */
    public List<UserSearchDocument> searchByEmail(String email) {
        return userSearchRepository.findByEmail(email);
    }
    
    /**
     * Поиск по телефону
     */
    public List<UserSearchDocument> searchByPhone(String phone) {
        return userSearchRepository.findByPhone(phone);
    }
    
    /**
     * Поиск по номеру счета
     */
    public List<UserSearchDocument> searchByAccountNumber(String accountNumber) {
        return userSearchRepository.findByAccountNumber(accountNumber);
    }
    
    /**
     * Поиск по городу
     */
    public List<UserSearchDocument> searchByCity(String cityName) {
        return userSearchRepository.findByCityName(cityName);
    }
    
    /**
     * Поиск по статусу
     */
    public List<UserSearchDocument> searchByStatus(String status) {
        return userSearchRepository.findByStatus(status);
    }
    
    /**
     * Комплексный поиск с использованием Criteria API
     */
    public List<UserSearchDocument> complexSearch(String searchTerm) {
        Criteria criteria = new Criteria("firstName").contains(searchTerm)
                .or("lastName").contains(searchTerm)
                .or("middleName").contains(searchTerm)
                .or("templates/email").is(searchTerm)
                .or("phone").is(searchTerm)
                .or("accountNumber").is(searchTerm);
        
        Query query = new CriteriaQuery(criteria);
        SearchHits<UserSearchDocument> searchHits = elasticsearchOperations.search(query, UserSearchDocument.class);
        
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
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



