package org.spacelab.housingutilitiessystemuser.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Document
public class Bill {
    @Id
    private String id;
    private String billNumber; 
    private LocalDate date; 

    @DocumentReference(lazy = true)
    private List<Receipt> receipt = new ArrayList<>();
}

