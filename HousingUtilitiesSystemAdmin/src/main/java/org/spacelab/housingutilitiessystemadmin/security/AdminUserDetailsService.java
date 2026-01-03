package org.spacelab.housingutilitiessystemadmin.security;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemadmin.entity.Admin;
import org.spacelab.housingutilitiessystemadmin.entity.Role;
import org.spacelab.housingutilitiessystemadmin.repository.AdminRepository;
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
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    /**
     * Загружает данные администратора по email для аутентификации.
     *
     * @param email Email администратора
     * @return Объект UserDetails с данными администратора
     * @throws UsernameNotFoundException если администратор не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByEmail(email);

        if (admin == null) {
            throw new UsernameNotFoundException("Администратор с email " + email + " не найден");
        }

        return new User(
                admin.getEmail(),
                admin.getPassword(),
                admin.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                mapRolesToAuthorities()
        );
    }

    /**
     * Преобразует роль администратора в формат GrantedAuthority.
     * Администраторы всегда имеют роль ADMIN.
     *
     * @return Коллекция прав доступа администратора
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + Role.ADMIN.name()));
    }
}
