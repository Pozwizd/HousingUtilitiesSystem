package org.spacelab.housingutilitiessystemuser.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.models.feedback.FeedbackRequestCreate;
import org.spacelab.housingutilitiessystemuser.models.feedback.FeedbackRequestResponse;
import org.spacelab.housingutilitiessystemuser.service.FeedbackService;
import org.spacelab.housingutilitiessystemuser.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestsRestController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Page<FeedbackRequestResponse> requestsPage = feedbackService.getRequestsForUser(user.getId(), page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("content", requestsPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", requestsPage.getTotalPages());
        response.put("totalElements", requestsPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackRequestResponse> getRequest(@PathVariable String id) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return feedbackService.getRequestById(id, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FeedbackRequestResponse> createRequest(@RequestBody FeedbackRequestCreate request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        FeedbackRequestResponse response = feedbackService.createRequest(request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteRequest(@PathVariable String id) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        boolean deleted = feedbackService.deleteRequest(id, user.getId());
        return ResponseEntity.ok(deleted);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Integer>> deleteRequests(@RequestBody List<String> ids) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        int deleted = feedbackService.deleteRequests(ids, user.getId());
        Map<String, Integer> response = new HashMap<>();
        response.put("deleted", deleted);
        return ResponseEntity.ok(response);
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
