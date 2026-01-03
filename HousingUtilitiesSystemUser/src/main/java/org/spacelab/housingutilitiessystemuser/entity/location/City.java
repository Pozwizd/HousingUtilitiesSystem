package org.spacelab.housingutilitiessystemuser.entity.location;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Data
@Document
public class City {
    @Id
    private String id;

    private String name;

    @DocumentReference(lazy = true)
    @JsonBackReference
    private Region region;

    @DocumentReference(lazy = true)
    @JsonManagedReference
    private List<Street> streets;
}
