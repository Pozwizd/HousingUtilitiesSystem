package org.spacelab.housingutilitiessystemadmin.controller.users;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
@Slf4j
public class UserViewController {

    @GetMapping({"/", ""})
    public ModelAndView getHorizontalPage(Model model) {
        return new ModelAndView("user/users")
                .addObject("pageTitle", "users.title")
                .addObject("pageActive", "users");
    }

    @GetMapping("/create")
    public ModelAndView getUserCreatePage(Model model) {
        model.addAttribute("pageTitle", "users.createUser");
        model.addAttribute("pageActive", "users");
        model.addAttribute("isEdit", false);
        model.addAttribute("opened", true);
        return new ModelAndView("user/user-edit");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView getUserEditPage(@PathVariable ObjectId id, Model model) {
        model.addAttribute("pageTitle", "users.editUser");
        model.addAttribute("pageActive", "users");
        model.addAttribute("isEdit", true);
        model.addAttribute("opened", true);
        return new ModelAndView("user/user-edit");
    }

    @GetMapping("/card/{id}")
    public ModelAndView showUserProfile(@PathVariable ObjectId id, Model model) {
        model.addAttribute("pageTitle", "users.userProfile");
        model.addAttribute("pageActive", "users");
        model.addAttribute("opened", true);
        return new ModelAndView("user/userCard");
    }
}
