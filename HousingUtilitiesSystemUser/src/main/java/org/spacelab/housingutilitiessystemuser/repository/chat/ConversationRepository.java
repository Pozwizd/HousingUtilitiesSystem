package org.spacelab.housingutilitiessystemuser.repository.chat;

import org.spacelab.housingutilitiessystemuser.entity.chat.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    
    @Query(value = "{'user': ObjectId(?0)}", sort = "{updatedAt: -1}")
    List<Conversation> findByUser(String userId);

    
    @Query(value = "{'chairman': ?0}", sort = "{updatedAt: -1}")
    List<Conversation> findByChairman(String chairmanId);

    
    @Query("{'chairman': ?0, 'user': ObjectId(?1)}")
    Optional<Conversation> findByChairmanIdAndUserId(String chairmanId, String userId);
}
