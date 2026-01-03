package org.spacelab.housingutilitiessystemadmin.entity;

import lombok.Data;
import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.spacelab.housingutilitiessystemadmin.entity.location.House;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Document
public class User {
    @Id
    private String id;

    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String email;

    @DocumentReference(lazy = true)
    private City city;
    @DocumentReference(lazy = true)
    private Street street;
    @DocumentReference(lazy = true)
    private House house;

    private String houseNumber;
    private String apartmentNumber;
    private Double apartmentArea;
    private String accountNumber;
    private Status status;
    private String password;
    private String login;
    private String photo;
    private Role role = Role.USER;
    private boolean online;
    private Instant lastActiveAt;
    private boolean enabled = true;

    @DocumentReference(lazy = true)
    private List<Bill> bills;

    @DocumentReference(lazy = true)
    private Set<Conversation> conversations = new HashSet<>();

    public String getFullName() {
        return lastName + " " + firstName + " " + middleName;
    }
}
