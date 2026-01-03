package org.spacelab.housingutilitiessystemadmin.security;

import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserRepository userRepository;

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("templates/email");
        String name = (String) attributes.get("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    String[] nameParts = name.split(" ");
                    if (nameParts.length > 0) {
                        newUser.setFirstName(nameParts[0]);
                    }
                    if (nameParts.length > 1) {
                        newUser.setLastName(nameParts[1]);
                    }
                    newUser.setPassword("");
                    userRepository.save(newUser);
                    return newUser;
                });

        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            String[] nameParts = name.split(" ");
            if (nameParts.length > 0) {
                user.setFirstName(nameParts[0]);
            }
            if (nameParts.length > 1) {
                user.setLastName(nameParts[1]);
            }
            userRepository.save(user);
        }

        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "templates/email"
        );
    }
}