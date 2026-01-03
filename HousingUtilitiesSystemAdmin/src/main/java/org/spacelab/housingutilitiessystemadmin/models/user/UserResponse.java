package org.spacelab.housingutilitiessystemadmin.models.user;

import lombok.Data;
import org.spacelab.housingutilitiessystemadmin.entity.Bill;
import org.spacelab.housingutilitiessystemadmin.models.location.CityResponse;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseResponse;
import org.spacelab.housingutilitiessystemadmin.models.location.StreetResponse;

import java.util.List;

@Data
public class UserResponse {
    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String email;
    private CityResponse city;
    private StreetResponse street;
    private HouseResponse house;
    private String apartmentNumber;
    private Double apartmentArea;
    private String accountNumber;
    private String status;
    private String photo;
    private String login;
    private List<Bill> bills;
    private String fullName;


}