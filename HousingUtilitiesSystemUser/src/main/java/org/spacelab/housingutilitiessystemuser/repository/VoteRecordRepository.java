package org.spacelab.housingutilitiessystemuser.repository;

import org.spacelab.housingutilitiessystemuser.entity.Vote;
import org.spacelab.housingutilitiessystemuser.entity.VoteRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRecordRepository extends MongoRepository<VoteRecord, String> {

    List<VoteRecord> findByVote(Vote vote);

    List<VoteRecord> findByVoteId(String voteId);

    Optional<VoteRecord> findByVoteIdAndUserId(String voteId, String userId);

    long countByVoteId(String voteId);
}
