package org.spacelab.housingutilitiessystemchairman.models.filters.chairman;
import lombok.Data;
@Data
public class ChairmanRequestTable {
    private int page = 0;
    private int size = 10;
    private String fullName;
    private String phone;
    private String email;
    private String login;
    private String status;
}
