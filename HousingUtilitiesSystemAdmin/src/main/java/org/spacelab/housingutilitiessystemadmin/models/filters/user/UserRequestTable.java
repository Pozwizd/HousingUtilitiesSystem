package org.spacelab.housingutilitiessystemadmin.models.filters.user;

import lombok.Data;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;

@Data
public class UserRequestTable {

    private int page = 0;
    private int size = 10;

    private String fullName;
    private String cityName;
    private String streetName;
    private String houseNumber;
    private String apartmentNumber;
    private String accountNumber;
    private String phoneNumber;
    private Status status;
}
