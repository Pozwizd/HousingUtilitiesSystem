package org.spacelab.housingutilitiessystemchairman.controller.pages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.service.ReceiptService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
@Slf4j
@Controller
@RequestMapping({"/accounts", "/accounts/"})
@RequiredArgsConstructor
public class ReceiptController {
    private final ReceiptService receiptService;
    @GetMapping
    public ModelAndView accountsPage() {
        log.debug("Opening accounts page");
        return new ModelAndView("accounts/accounts")
                .addObject("pageActive", "accounts")
                .addObject("pageTitle", "pages.accounts");
    }
}
