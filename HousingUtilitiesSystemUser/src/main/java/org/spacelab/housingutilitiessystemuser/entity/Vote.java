package org.spacelab.housingutilitiessystemuser.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class Vote {
    @Id
    private String id;
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private Double quorumArea;
    private String status;

    private Integer forVotesCount = 0;
    private Integer againstVotesCount = 0;
    private Integer abstentionsCount = 0;
}
