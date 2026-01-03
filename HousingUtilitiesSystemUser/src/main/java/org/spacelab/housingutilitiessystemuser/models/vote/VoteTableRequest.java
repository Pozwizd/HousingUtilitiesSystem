package org.spacelab.housingutilitiessystemuser.models.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteTableRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String title;
    private String status;
    private String result;
}
