package org.spacelab.housingutilitiessystemchairman.repository.mongo.chat;

import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemchairman.entity.chat.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    @Query(value = "{'conversation': ObjectId(?0)}", sort = "{'createdAt': -1}")
    List<ChatMessage> findByConversationId(String conversationId, Pageable pageable);
    @Query(value = "{'conversation': ObjectId(?0)}", sort = "{'createdAt': -1}")
    List<ChatMessage> findLatestByConversationId(String conversationId);

    @Query(value = "{'conversation': ObjectId(?0)}", sort = "{'createdAt': -1}")
    Optional<ChatMessage> findFirstByConversationIdOrderByCreatedAtDesc(String conversationId);

    @Query(value = "{'conversation': {$in: ?0}}", sort = "{'createdAt': -1}")
    List<ChatMessage> findByConversationIdIn(List<ObjectId> conversationIds);
}
