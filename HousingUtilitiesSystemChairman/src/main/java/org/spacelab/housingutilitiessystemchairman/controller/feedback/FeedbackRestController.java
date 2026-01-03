package org.spacelab.housingutilitiessystemchairman.controller.feedback;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.feedback.FeedbackDetailResponse;
import org.spacelab.housingutilitiessystemchairman.models.feedback.FeedbackResponseTable;
import org.spacelab.housingutilitiessystemchairman.models.filters.feedback.FeedbackRequestTable;
import org.spacelab.housingutilitiessystemchairman.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
@AllArgsConstructor
@Slf4j
public class FeedbackRestController {
    private final FeedbackService feedbackService;

    @PostMapping("/getAll")
    public ResponseEntity<PageResponse<FeedbackResponseTable>> getFeedbackTable(
            @Valid @RequestBody FeedbackRequestTable request) {
        return ResponseEntity.ok(feedbackService.getFeedbackTable(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDetailResponse> getFeedback(@PathVariable String id) {
        return ResponseEntity.ok(feedbackService.getFeedbackById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteFeedback(@PathVariable String id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(true);
    }
}
