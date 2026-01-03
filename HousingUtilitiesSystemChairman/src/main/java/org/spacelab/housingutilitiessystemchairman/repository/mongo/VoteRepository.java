package org.spacelab.housingutilitiessystemchairman.repository.mongo;
import org.spacelab.housingutilitiessystemchairman.entity.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {
}
