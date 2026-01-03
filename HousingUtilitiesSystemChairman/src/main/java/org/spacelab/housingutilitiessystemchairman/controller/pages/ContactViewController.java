package org.spacelab.housingutilitiessystemchairman.controller.pages;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/contacts")
@AllArgsConstructor
@Slf4j
public class ContactViewController {
    @GetMapping({ "", "/" })
    public ModelAndView getContactsPage(Model model) {
        return new ModelAndView("contact/contacts")
                .addObject("pageTitle", "contacts.title")
                .addObject("pageActive", "contacts");
    }
}
