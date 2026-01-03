package org.spacelab.housingutilitiessystemadmin.models.user;

import lombok.Data;
import org.spacelab.housingutilitiessystemadmin.entity.User;

import java.io.Serializable;

/**
 * UserResponseTable for {@link User}
 */
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