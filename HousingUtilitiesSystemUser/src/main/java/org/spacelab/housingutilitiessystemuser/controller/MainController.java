package org.spacelab.housingutilitiessystemuser.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    @GetMapping({ "/", "" })
    public String index(Model model) {
        User user = getCurrentUser();

        if (user != null) {
            model.addAttribute("user", user);
        }

        model.addAttribute("pageActive", "profile");
        model.addAttribute("pageTitle", "Профиль");
        return "profile";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            return userService.findByEmail(username)
                    .or(() -> userService.findByLogin(username))
                    .orElse(null);
        }
        return null;
    }
}
