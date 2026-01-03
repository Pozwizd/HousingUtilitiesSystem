package org.spacelab.housingutilitiessystemadmin.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

/**
 * Запись о голосовании конкретного пользователя
 * Отдельная коллекция для оптимизации производительности
 */
@Data
@Document
public class VoteRecord {
    @Id
    private String id;
    
    @DocumentReference
    private Vote vote;
    
    @DocumentReference
    private User user;
    
    /**
     * Тип голоса: "FOR", "AGAINST", "ABSTENTION"
     */
    private String voteType;
    
    private java.util.Date voteTime;
}
