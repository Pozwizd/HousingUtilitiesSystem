package org.spacelab.housingutilitiessystemadmin.entity.location;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.spacelab.housingutilitiessystemadmin.entity.Chairman;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
import java.util.List;

@Data
@Document
public class House {
    @Id
    private String id;

    private String houseNumber;

    @DocumentReference(lazy = true)
    @JsonBackReference
    private Street street;

    @DocumentReference(lazy = true)
    private Chairman chairman;

    @DocumentReference(lazy = true)
    private List<User> residents = new ArrayList<>();

    private Status status;
}