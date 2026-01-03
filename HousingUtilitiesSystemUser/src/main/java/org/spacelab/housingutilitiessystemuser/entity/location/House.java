package org.spacelab.housingutilitiessystemuser.entity.location;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.ToString;

import org.spacelab.housingutilitiessystemuser.entity.Chairman;
import org.spacelab.housingutilitiessystemuser.entity.User;
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
    @ToString.Exclude
    private Street street;

    @DocumentReference(lazy = true)
    @ToString.Exclude
    private Chairman chairman;

    @DocumentReference(lazy = true)
    @ToString.Exclude
    private List<User> residents = new ArrayList<>();

    private Status status;
}
