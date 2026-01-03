package org.spacelab.housingutilitiessystemuser.controller;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final UserRepository userRepository;

    @GetMapping({ "/chat", "/chat/" })
    public ModelAndView chatPage() {
        
        String currentUserId = getCurrentUserId();

        return new ModelAndView("chat/chat")
                .addObject("pageTitle", "pages.chat")
                .addObject("pageActive", "chat")
                .addObject("currentUserId", currentUserId);
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            return userRepository.findByEmail(username)
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }
}
