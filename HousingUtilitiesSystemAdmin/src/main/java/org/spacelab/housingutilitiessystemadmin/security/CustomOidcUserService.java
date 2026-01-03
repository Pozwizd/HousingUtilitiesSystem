package org.spacelab.housingutilitiessystemadmin.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.Admin;
import org.spacelab.housingutilitiessystemadmin.entity.Role;
import org.spacelab.housingutilitiessystemadmin.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== OIDC USER LOADING STARTED ===");
        
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();

        log.info("OIDC User email: {}", email);
        log.info("OIDC User name: {}", name);
        log.info("OIDC User picture: {}", picture);

        if (email == null || email.isEmpty()) {
            log.error("Email not found from OIDC provider");
            throw new OAuth2AuthenticationException("Email not found from OIDC provider");
        }

        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            log.info("Creating new admin for email: {}", email);
            admin = new Admin();
            admin.setEmail(email);
            admin.setUsername(name != null && !name.isEmpty() ? name : email);
            admin.setPathAvatar(picture);
            admin.setRole(Role.ADMIN);
            admin.setPassword(passwordEncoder.encode(""));
            admin.setEnabled(true);
            
            admin = adminRepository.save(admin);
        }

        if (picture != null && !picture.equals(admin.getPathAvatar())) {
            log.info("Updating admin avatar for email: {}", email);
            admin.setPathAvatar(picture);
            adminRepository.save(admin);
        }

        if (name != null && !name.equals(admin.getUsername())) {
            log.info("Updating admin username for email: {}", email);
            admin.setUsername(name);
            adminRepository.save(admin);
        }

        log.info("Admin loaded successfully: ID={}, Role={}", admin.getId(), admin.getRole());
        return new CustomOidcUser(oidcUser, admin);
    }
}
