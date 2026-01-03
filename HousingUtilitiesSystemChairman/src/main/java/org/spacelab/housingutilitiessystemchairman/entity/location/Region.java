package org.spacelab.housingutilitiessystemchairman.entity.location;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;
@Data
@Document
public class Region {
    @Id
    private String id;
    private String name;
    @DocumentReference(lazy = true)
    @JsonManagedReference
    private List<City> cities;
}
