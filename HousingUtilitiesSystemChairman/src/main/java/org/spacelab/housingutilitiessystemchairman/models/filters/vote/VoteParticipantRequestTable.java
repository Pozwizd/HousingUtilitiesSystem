package org.spacelab.housingutilitiessystemchairman.models.filters.vote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteParticipantRequestTable {
    private String voteId;
    private String fullName;
    private String apartmentNumber;
    private String phone;
    private String voteType;
    private int page = 0;
    private int size = 10;
}
