package org.spacelab.housingutilitiessystemuser.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.models.contact.ContactSectionResponse;
import org.spacelab.housingutilitiessystemuser.service.ContactService;
import org.spacelab.housingutilitiessystemuser.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final UserService userService;

    @GetMapping({ "", "/" })
    public String getContactsPage(Model model) {
        User user = getCurrentUser();
        if (user != null) {
            model.addAttribute("user", user);
        }
        model.addAttribute("pageActive", "contacts");
        model.addAttribute("pageTitle", "Контакты");
        return "contacts/contacts";
    }

    @GetMapping("/api/sections")
    @ResponseBody
    public ResponseEntity<List<ContactSectionResponse>> getSections() {
        List<ContactSectionResponse> sections = contactService.getAllSections();
        return ResponseEntity.ok(sections);
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
