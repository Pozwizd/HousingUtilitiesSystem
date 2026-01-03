package org.spacelab.housingutilitiessystemuser.repository.chat;

import org.spacelab.housingutilitiessystemuser.entity.chat.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    
    @Query(value = "{'conversation': ObjectId(?0)}", sort = "{'createdAt': -1}")
    List<ChatMessage> findByConversationId(String conversationId, Pageable pageable);

    
    @Query(value = "{'conversation': ObjectId(?0)}", sort = "{'createdAt': -1}")
    List<ChatMessage> findLatestByConversationId(String conversationId);
}
