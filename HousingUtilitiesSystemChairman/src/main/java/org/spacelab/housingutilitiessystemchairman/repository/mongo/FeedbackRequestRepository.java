package org.spacelab.housingutilitiessystemchairman.repository.mongo;

import org.spacelab.housingutilitiessystemchairman.entity.FeedbackRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackRequestRepository extends MongoRepository<FeedbackRequest, String> {
    Page<FeedbackRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
    Page<FeedbackRequest> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    @Query("{ 'createdAt': { $gte: ?0 } }")
    Page<FeedbackRequest> findByCreatedAtAfterOrderByCreatedAtDesc(
            LocalDateTime dateFrom, Pageable pageable);

    @Query("{ 'createdAt': { $lte: ?0 } }")
    Page<FeedbackRequest> findByCreatedAtBeforeOrderByCreatedAtDesc(
            LocalDateTime dateTo, Pageable pageable);

    List<FeedbackRequest> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserId(String userId);
}
