package org.spacelab.housingutilitiessystemchairman.controller.votes;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Vote;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.filters.vote.VoteParticipantRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.filters.vote.VoteRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteDetailResponse;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteParticipantResponse;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteRequest;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteResponseTable;
import org.spacelab.housingutilitiessystemchairman.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
@RestController
@RequestMapping("/voting")
@AllArgsConstructor
@Slf4j
public class VoteRestController {
    private final VoteService voteService;
    @PostMapping("/getAll")
    public ResponseEntity<PageResponse<VoteResponseTable>> getVotesTable(
            @Valid @RequestBody VoteRequestTable request) {
        return ResponseEntity.ok(voteService.getVotesTable(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<VoteResponseTable> getVote(@PathVariable String id) {
        return ResponseEntity.ok(voteService.getVoteById(id));
    }
    @PostMapping("/create")
    public ResponseEntity<Vote> createVote(@Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(voteService.createVote(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Vote> updateVote(
            @PathVariable String id,
            @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(voteService.updateVote(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteVote(@PathVariable String id) {
        voteService.deleteById(id);
        return ResponseEntity.ok(true);
    }
    @GetMapping("/getStatuses")
    public ResponseEntity<List<String>> getStatuses() {
        return ResponseEntity.ok(Arrays.asList("Активное", "Закрыто"));
    }
    @GetMapping("/getResults")
    public ResponseEntity<List<String>> getResults() {
        return ResponseEntity.ok(Arrays.asList("Принято", "Отклонено"));
    }
    @GetMapping("/{id}/detail")
    public ResponseEntity<VoteDetailResponse> getVoteDetail(@PathVariable String id) {
        return ResponseEntity.ok(voteService.getVoteDetail(id));
    }
    @PostMapping("/{id}/participants")
    public ResponseEntity<PageResponse<VoteParticipantResponse>> getVoteParticipants(
            @PathVariable String id,
            @Valid @RequestBody VoteParticipantRequestTable request) {
        request.setVoteId(id);
        return ResponseEntity.ok(voteService.getVoteParticipants(request));
    }
}
