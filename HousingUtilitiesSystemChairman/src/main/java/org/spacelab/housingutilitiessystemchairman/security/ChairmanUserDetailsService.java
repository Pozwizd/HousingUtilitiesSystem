package org.spacelab.housingutilitiessystemchairman.security;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.Role;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
@Service
@RequiredArgsConstructor
public class ChairmanUserDetailsService implements UserDetailsService {
    private final ChairmanRepository chairmanRepository;
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Chairman chairman = chairmanRepository.findByEmail(login)
                .or(() -> chairmanRepository.findByLogin(login))
                .orElseThrow(() -> new UsernameNotFoundException("Председатель с login " + login + " не найден"));
        return new User(
                chairman.getLogin(),
                chairman.getPassword(),
                chairman.isEnabled(),
                true,
                true,
                true,
                mapRolesToAuthorities(chairman.getRole())
        );
    }
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Role role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
