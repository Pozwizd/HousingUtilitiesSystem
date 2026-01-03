package org.spacelab.housingutilitiessystemchairman.models.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteDetailResponse {
    private String id;
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private Double quorumArea;
    private String status;
    private String result;
    private Integer forVotesCount;
    private Integer againstVotesCount;
    private Integer abstentionsCount;
    private Double forVotesArea;
    private Double againstVotesArea;
    private Double abstentionsArea;
    private Double totalVotedArea;
}
