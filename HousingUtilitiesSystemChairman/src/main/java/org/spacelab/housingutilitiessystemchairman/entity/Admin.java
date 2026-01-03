package org.spacelab.housingutilitiessystemchairman.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
@Data
@Document
public class Admin implements UserDetails {
    @Id
    private String id;
    private String email;
    private String password;
    private String username;
    private String pathAvatar;
    private Role role = Role.ADMIN;
    private boolean enabled = true;
    private String phone;
    private String organization;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String language;
    private String timezone;
    private String currency;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override
    public String getUsername() {
        return email;
    }
    public String getDisplayName() {
        return username != null && !username.isEmpty() ? username : email;
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
