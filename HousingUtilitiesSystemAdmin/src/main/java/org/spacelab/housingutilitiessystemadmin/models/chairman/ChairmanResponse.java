package org.spacelab.housingutilitiessystemadmin.models.chairman;

import lombok.Data;

@Data
public class ChairmanResponse {
    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String email;
    private String status;
    private String login;
    private String photo;
    private String fullName;
}

