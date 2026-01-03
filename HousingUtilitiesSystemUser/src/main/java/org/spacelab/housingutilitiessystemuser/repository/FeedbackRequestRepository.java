package org.spacelab.housingutilitiessystemuser.repository;

import org.spacelab.housingutilitiessystemuser.entity.FeedbackRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRequestRepository extends MongoRepository<FeedbackRequest, String> {

    Page<FeedbackRequest> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<FeedbackRequest> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserId(String userId);
}
