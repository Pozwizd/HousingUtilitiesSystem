package org.spacelab.housingutilitiessystemchairman.security;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.entity.Role;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
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
    private final ChairmanRepository chairmanRepository;
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
        Chairman chairman = chairmanRepository.findByEmail(email)
                .orElse(null);
        if (chairman == null) {
            log.info("Creating new chairman for email: {}", email);
            chairman = new Chairman();
            chairman.setEmail(email);
            chairman.setLogin(email);
            if (name != null && !name.isEmpty()) {
                String[] nameParts = name.split(" ");
                if (nameParts.length > 0) chairman.setLastName(nameParts[0]);
                if (nameParts.length > 1) chairman.setFirstName(nameParts[1]);
                if (nameParts.length > 2) chairman.setMiddleName(nameParts[2]);
            }
            chairman.setPhoto(picture);
            chairman.setRole(Role.USER);
            chairman.setPassword(passwordEncoder.encode(""));
            chairman.setEnabled(true);
            chairman = chairmanRepository.save(chairman);
        }
        if (picture != null && !picture.equals(chairman.getPhoto())) {
            log.info("Updating chairman photo for email: {}", email);
            chairman.setPhoto(picture);
            chairmanRepository.save(chairman);
        }
        if (name != null) {
            String[] nameParts = name.split(" ");
            boolean needsUpdate = false;
            if (nameParts.length > 0 && !nameParts[0].equals(chairman.getLastName())) {
                chairman.setLastName(nameParts[0]);
                needsUpdate = true;
            }
            if (nameParts.length > 1 && !nameParts[1].equals(chairman.getFirstName())) {
                chairman.setFirstName(nameParts[1]);
                needsUpdate = true;
            }
            if (nameParts.length > 2 && !nameParts[2].equals(chairman.getMiddleName())) {
                chairman.setMiddleName(nameParts[2]);
                needsUpdate = true;
            }
            if (needsUpdate) {
                log.info("Updating chairman name for email: {}", email);
                chairmanRepository.save(chairman);
            }
        }
        log.info("Chairman loaded successfully: ID={}, Role={}", chairman.getId(), chairman.getRole());
        return new CustomOidcUser(oidcUser, chairman);
    }
}
