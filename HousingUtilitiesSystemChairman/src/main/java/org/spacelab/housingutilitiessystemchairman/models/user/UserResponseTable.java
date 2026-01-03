package org.spacelab.housingutilitiessystemchairman.models.user;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserResponseTable implements Serializable {
    String id;
    String fullName;
    String cityName;
    String streetName;
    String houseNumber;
    String apartmentNumber;
    String accountNumber;
    String phoneNumber;
    String status;
}