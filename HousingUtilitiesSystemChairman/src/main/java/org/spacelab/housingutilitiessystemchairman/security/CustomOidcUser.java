package org.spacelab.housingutilitiessystemchairman.security;

import lombok.Getter;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;
@Getter
public class CustomOidcUser implements OidcUser, UserDetails {
    private final OidcUser oidcUser;
    private final Chairman chairman;
    public CustomOidcUser(OidcUser oidcUser, Chairman chairman) {
        this.oidcUser = oidcUser;
        this.chairman = chairman;
    }
    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }
    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }
    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }
    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return chairman.getAuthorities();
    }
    @Override
    public String getName() {
        return oidcUser.getName();
    }
    public String getEmail() {
        return oidcUser.getEmail();
    }
    @Override
    public String getPassword() {
        return chairman.getPassword();
    }
    @Override
    public String getUsername() {
        return chairman.getLogin();
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
        return true;
    }
}
