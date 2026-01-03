package org.spacelab.housingutilitiessystemuser.models.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CastVoteRequest {
    
    private String voteType;
}
