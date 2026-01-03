package org.spacelab.housingutilitiessystemuser.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.Role;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
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

    private final UserRepository userRepository;
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

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            log.info("Creating new user for email: {}", email);
            user = new User();
            user.setEmail(email);
            user.setLogin(email);

            if (name != null && !name.isEmpty()) {
                String[] nameParts = name.split(" ");
                if (nameParts.length > 0)
                    user.setLastName(nameParts[0]);
                if (nameParts.length > 1)
                    user.setFirstName(nameParts[1]);
                if (nameParts.length > 2)
                    user.setMiddleName(nameParts[2]);
            }

            user.setPhoto(picture);
            user.setRole(Role.USER);
            user.setPassword(passwordEncoder.encode(""));
            user.setEnabled(true);

            user = userRepository.save(user);
        }

        if (picture != null && !picture.equals(user.getPhoto())) {
            log.info("Updating user photo for email: {}", email);
            user.setPhoto(picture);
            userRepository.save(user);
        }

        if (name != null) {
            String[] nameParts = name.split(" ");
            boolean needsUpdate = false;

            if (nameParts.length > 0 && !nameParts[0].equals(user.getLastName())) {
                user.setLastName(nameParts[0]);
                needsUpdate = true;
            }
            if (nameParts.length > 1 && !nameParts[1].equals(user.getFirstName())) {
                user.setFirstName(nameParts[1]);
                needsUpdate = true;
            }
            if (nameParts.length > 2 && !nameParts[2].equals(user.getMiddleName())) {
                user.setMiddleName(nameParts[2]);
                needsUpdate = true;
            }

            if (needsUpdate) {
                log.info("Updating user name for email: {}", email);
                userRepository.save(user);
            }
        }

        log.info("User loaded successfully: ID={}, Role={}", user.getId(), user.getRole());
        return new CustomOidcUser(oidcUser, user);
    }
}
