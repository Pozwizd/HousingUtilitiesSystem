package org.spacelab.housingutilitiessystemchairman.controller.pages;
import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
@Controller
@RequiredArgsConstructor
public class ChatPageController {
    private final ChairmanRepository chairmanRepository;
    @GetMapping({ "/chat", "/chat/" })
    public ModelAndView chatPage() {
        String currentUserId = getCurrentChairmanId();
        return new ModelAndView("chat/chat")
                .addObject("pageTitle", "pages.chat")
                .addObject("pageActive", "chat")
                .addObject("currentUserId", currentUserId);
    }
    private String getCurrentChairmanId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            return chairmanRepository.findByLogin(username)
                    .map(Chairman::getId)
                    .orElse(null);
        }
        return null;
    }
}
