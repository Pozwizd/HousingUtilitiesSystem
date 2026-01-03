package org.spacelab.housingutilitiessystemadmin.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, String> {
    
    List<UserSearchDocument> findByFirstNameContainingOrLastNameContainingOrMiddleNameContaining(
            String firstName, String lastName, String middleName);
    
    List<UserSearchDocument> findByEmail(String email);
    
    List<UserSearchDocument> findByPhone(String phone);
    
    List<UserSearchDocument> findByAccountNumber(String accountNumber);
    
    List<UserSearchDocument> findByCityName(String cityName);
    
    List<UserSearchDocument> findByStatus(String status);
}
