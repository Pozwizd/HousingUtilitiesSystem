package org.spacelab.housingutilitiessystemadmin.security;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "remember_me_tokens")
public class RememberMeTokenDocument {

    @Id
    private String id;
    private String username;
    private String series;
    private String tokenValue;
    private Date date;
}
