package org.spacelab.housingutilitiessystemchairman.models.chairman;

import lombok.Data;

import java.io.Serializable;
@Data
public class ChairmanResponseTable implements Serializable {
    private String id;
    private String fullName;
    private String phone;
    private String email;
    private String login;
    private String status;
}
