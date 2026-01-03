package org.spacelab.housingutilitiessystemuser.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping({ "", "/" })
    public String getProfilePage() {
        
        return "redirect:/";
    }
}
