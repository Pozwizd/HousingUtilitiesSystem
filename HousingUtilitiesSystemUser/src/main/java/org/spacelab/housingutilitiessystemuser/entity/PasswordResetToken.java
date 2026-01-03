package org.spacelab.housingutilitiessystemuser.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
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

    @DocumentReference(lazy = true)
    private User user;

    private LocalDateTime calculateExpirationDate() {
        return LocalDateTime.now().plusMinutes(EXPIRATION);
    }

    public void setExpirationDate() {
        this.expirationDate = LocalDateTime.now().plusMinutes(EXPIRATION);
    }

    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.expirationDate = calculateExpirationDate();
        this.user = user;
    }
}
