package org.spacelab.housingutilitiessystemchairman.entity.location;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.User;
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
    @EqualsAndHashCode.Exclude
    private Street street;
    @DocumentReference(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Chairman chairman;
    @DocumentReference(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> residents = new ArrayList<>();
    private Status status;
}