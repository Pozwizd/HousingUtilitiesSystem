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
@RequestMapping("/voting")
@AllArgsConstructor
@Slf4j
public class VoteViewController {
    @GetMapping({ "", "/" })
    public ModelAndView getVotesPage(Model model) {
        return new ModelAndView("vote/votes")
                .addObject("pageTitle", "voting.title")
                .addObject("pageActive", "voting");
    }
    @GetMapping("/{id}/view")
    public ModelAndView getVoteDetailPage(@PathVariable String id, Model model) {
        return new ModelAndView("vote/vote-detail")
                .addObject("pageTitle", "voting.voteDetail")
                .addObject("pageActive", "voting")
                .addObject("voteId", id);
    }
}
