package org.spacelab.housingutilitiessystemadmin.repository;

import org.spacelab.housingutilitiessystemadmin.entity.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {
}