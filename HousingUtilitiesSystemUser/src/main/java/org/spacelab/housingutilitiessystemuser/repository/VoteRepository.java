package org.spacelab.housingutilitiessystemuser.repository;

import org.spacelab.housingutilitiessystemuser.entity.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {
}
