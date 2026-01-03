package org.spacelab.housingutilitiessystemchairman.entity;

import lombok.*;
import lombok.Builder.Default;
import org.spacelab.housingutilitiessystemchairman.entity.chat.Conversation;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.entity.location.Status;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chairman implements UserDetails {
    @Id
    private String id;
    private String lastName;
    private String firstName;
    private String middleName;
    private String phone;
    private String email;
    private Status status;
    private String login;
    private String password;
    private String photo;
    @Default
    private Role role = Role.USER;
    @Default
    private boolean enabled = true;
    @Default
    private boolean online = false;
    private Instant lastActiveAt;
    @DocumentReference(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private House house;
    @DocumentReference(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Default
    private Set<Conversation> conversations = new HashSet<>();
    public String getFullName() {
        return lastName + " " + firstName + " " + middleName;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override
    public String getUsername() {
        return login;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
