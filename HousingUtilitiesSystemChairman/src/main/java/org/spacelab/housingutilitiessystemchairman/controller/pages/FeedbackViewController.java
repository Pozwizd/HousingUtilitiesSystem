package org.spacelab.housingutilitiessystemchairman.controller.pages;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/feedback")
@AllArgsConstructor
@Slf4j
public class FeedbackViewController {
    @GetMapping({"", "/"})
    public ModelAndView getFeedbackPage(Model model) {
        return new ModelAndView("feedback/feedback")
                .addObject("pageTitle", "feedback.title")
                .addObject("pageActive", "feedback");
    }

    @GetMapping("/{id}/view")
    public ModelAndView getFeedbackDetailPage(@PathVariable String id, Model model) {
        return new ModelAndView("feedback/feedback-detail")
                .addObject("pageTitle", "feedback.feedbackDetail")
                .addObject("pageActive", "feedback")
                .addObject("feedbackId", id);
    }
}
