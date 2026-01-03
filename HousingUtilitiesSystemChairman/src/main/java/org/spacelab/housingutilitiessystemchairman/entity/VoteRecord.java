package org.spacelab.housingutilitiessystemchairman.entity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
@Data
@Document
public class VoteRecord {
    @Id
    private String id;
    @DocumentReference
    private Vote vote;
    @DocumentReference
    private User user;
    private String voteType;
    private java.util.Date voteTime;
}
