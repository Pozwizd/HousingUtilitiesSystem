package org.spacelab.housingutilitiessystemchairman.models.vote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteParticipantResponse {
    private String id;
    private String fullName;
    private String apartmentNumber;
    private Double apartmentArea;
    private String phone;
    private String voteType;
    private String voteTypeDisplay;
    private java.util.Date voteTime;
}
