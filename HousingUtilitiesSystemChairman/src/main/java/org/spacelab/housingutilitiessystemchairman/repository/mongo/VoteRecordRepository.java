package org.spacelab.housingutilitiessystemchairman.repository.mongo;

import org.spacelab.housingutilitiessystemchairman.entity.Vote;
import org.spacelab.housingutilitiessystemchairman.entity.VoteRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface VoteRecordRepository extends MongoRepository<VoteRecord, String> {
    List<VoteRecord> findByVote(Vote vote);
    List<VoteRecord> findByVoteId(String voteId);
    long countByVoteId(String voteId);
}
