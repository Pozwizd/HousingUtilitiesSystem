package org.spacelab.housingutilitiessystemchairman.models.filters.vote;
import lombok.Data;
@Data
public class VoteRequestTable {
    private int page = 0;
    private int size = 10;
    private String title;
    private String status;
    private String result;
}
