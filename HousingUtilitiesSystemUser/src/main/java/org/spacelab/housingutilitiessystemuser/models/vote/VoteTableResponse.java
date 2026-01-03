package org.spacelab.housingutilitiessystemuser.models.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteTableResponse {
    private String id;
    private String title;
    private Date endTime;
    private String status;
    private String result;
    private Integer forVotesCount;
    private Integer againstVotesCount;
    private Integer abstentionsCount;
    private Double totalVotedArea;
}
