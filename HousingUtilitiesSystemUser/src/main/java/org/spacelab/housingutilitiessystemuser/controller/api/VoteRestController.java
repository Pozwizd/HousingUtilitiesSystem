package org.spacelab.housingutilitiessystemuser.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.models.PageResponse;
import org.spacelab.housingutilitiessystemuser.models.vote.CastVoteRequest;
import org.spacelab.housingutilitiessystemuser.models.vote.VoteDetailResponse;
import org.spacelab.housingutilitiessystemuser.models.vote.VoteTableRequest;
import org.spacelab.housingutilitiessystemuser.models.vote.VoteTableResponse;
import org.spacelab.housingutilitiessystemuser.service.UserService;
import org.spacelab.housingutilitiessystemuser.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/voting")
@RequiredArgsConstructor
public class VoteRestController {

    private final VoteService voteService;
    private final UserService userService;

    
    @PostMapping("/getAll")
    public ResponseEntity<PageResponse<VoteTableResponse>> getVotesTable(@RequestBody VoteTableRequest request) {
        PageResponse<VoteTableResponse> response = voteService.getVotesTable(request);
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/{id}/detail")
    public ResponseEntity<VoteDetailResponse> getVoteDetail(@PathVariable String id) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        VoteDetailResponse response = voteService.getVoteDetail(id, user.getId());
        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/{id}/vote")
    public ResponseEntity<?> castVote(@PathVariable String id, @RequestBody CastVoteRequest request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            voteService.castVote(id, user.getId(), request.getVoteType());
            return ResponseEntity.ok(Map.of("success", true, "message", "Голос успешно учтен"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
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
