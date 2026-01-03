package org.spacelab.housingutilitiessystemuser.security;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemuser.entity.Role;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(login)
                .or(() -> userRepository.findByLogin(login))
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с login " + login + " не найден"));

        return new org.springframework.security.core.userdetails.User(
                user.getLogin() != null ? user.getLogin() : user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true, 
                true, 
                true, 
                mapRolesToAuthorities(user.getRole()));
    }

    
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Role role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
