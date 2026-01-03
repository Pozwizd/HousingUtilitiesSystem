package org.spacelab.housingutilitiessystemadmin.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;



@Data
@NoArgsConstructor
@Document
public class PasswordResetToken {
    @Id
    private String id;
    private String token;
    private LocalDateTime expirationDate;
    private static final int EXPIRATION = 10;

    @DocumentReference(
            lazy = true
    )
    private Admin adminUser;


    private LocalDateTime calculateExpirationDate() {
        return LocalDateTime.now().plusMinutes(EXPIRATION);
    }

    public void setExpirationDate() {
        this.expirationDate = LocalDateTime.now().plusMinutes(EXPIRATION);
    }

    public PasswordResetToken(String token, Admin adminUser) {
        this.token = token;
        this.expirationDate = calculateExpirationDate();
        this.adminUser = adminUser;
    }

}
