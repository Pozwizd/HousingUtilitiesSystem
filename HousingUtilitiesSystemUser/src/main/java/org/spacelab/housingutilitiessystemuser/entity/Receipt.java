package org.spacelab.housingutilitiessystemuser.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDate;


@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    @Id
    private String id;

    @DocumentReference(lazy = true)
    private Bill bill;

    private LocalDate localDate;
}
