package org.spacelab.housingutilitiessystemuser.service;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.entity.Vote;
import org.spacelab.housingutilitiessystemuser.entity.VoteRecord;
import org.spacelab.housingutilitiessystemuser.models.PageResponse;
import org.spacelab.housingutilitiessystemuser.models.vote.VoteDetailResponse;
import org.spacelab.housingutilitiessystemuser.models.vote.VoteTableRequest;
import org.spacelab.housingutilitiessystemuser.models.vote.VoteTableResponse;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.spacelab.housingutilitiessystemuser.repository.VoteRecordRepository;
import org.spacelab.housingutilitiessystemuser.repository.VoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    
    public PageResponse<VoteTableResponse> getVotesTable(VoteTableRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10);

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            criteriaList.add(Criteria.where("title").regex(request.getTitle(), "i"));
        }

        
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            criteriaList.add(Criteria.where("status").is(request.getStatus()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Vote.class);

        query.with(pageable);
        List<Vote> votes = mongoTemplate.find(query, Vote.class);

        
        List<VoteTableResponse> responseList = votes.stream()
                .map(this::mapToTableResponse)
                .collect(Collectors.toList());

        
        if (request.getResult() != null && !request.getResult().isEmpty()) {
            responseList = responseList.stream()
                    .filter(v -> request.getResult().equals(v.getResult()))
                    .collect(Collectors.toList());
        }

        Page<VoteTableResponse> page = new PageImpl<>(responseList, pageable, total);
        return PageResponse.of(page);
    }

    
    public VoteDetailResponse getVoteDetail(String voteId, String userId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found: " + voteId));

        
        List<VoteRecord> records = voteRecordRepository.findByVoteId(voteId);

        double forArea = 0.0;
        double againstArea = 0.0;
        double abstainArea = 0.0;

        for (VoteRecord record : records) {
            if (record.getUser() != null && record.getUser().getApartmentArea() != null) {
                double area = record.getUser().getApartmentArea();
                switch (record.getVoteType()) {
                    case "FOR" -> forArea += area;
                    case "AGAINST" -> againstArea += area;
                    case "ABSTENTION" -> abstainArea += area;
                }
            }
        }

        
        Optional<VoteRecord> userVote = voteRecordRepository.findByVoteIdAndUserId(voteId, userId);
        boolean userHasVoted = userVote.isPresent();
        String userVoteType = userHasVoted ? userVote.get().getVoteType() : null;
        String userVoteTypeDisplay = getVoteTypeDisplay(userVoteType);

        
        String result = determineResult(vote);

        return VoteDetailResponse.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .description(vote.getDescription())
                .startTime(vote.getStartTime())
                .endTime(vote.getEndTime())
                .quorumArea(vote.getQuorumArea())
                .status(vote.getStatus())
                .result(result)
                .forVotesCount(vote.getForVotesCount())
                .againstVotesCount(vote.getAgainstVotesCount())
                .abstentionsCount(vote.getAbstentionsCount())
                .forVotesArea(forArea)
                .againstVotesArea(againstArea)
                .abstentionsArea(abstainArea)
                .totalVotedArea(forArea + againstArea + abstainArea)
                .userHasVoted(userHasVoted)
                .userVoteType(userVoteType)
                .userVoteTypeDisplay(userVoteTypeDisplay)
                .build();
    }

    
    public void castVote(String voteId, String userId, String voteType) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found: " + voteId));

        
        if (!"Активное".equals(vote.getStatus())) {
            throw new RuntimeException("Голосование закрыто");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        
        Optional<VoteRecord> existingVote = voteRecordRepository.findByVoteIdAndUserId(voteId, userId);

        if (existingVote.isPresent()) {
            
            VoteRecord record = existingVote.get();
            String oldVoteType = record.getVoteType();

            
            decrementVoteCount(vote, oldVoteType);
            incrementVoteCount(vote, voteType);

            
            record.setVoteType(voteType);
            record.setVoteTime(new Date());
            voteRecordRepository.save(record);
        } else {
            
            VoteRecord record = new VoteRecord();
            record.setVote(vote);
            record.setUser(user);
            record.setVoteType(voteType);
            record.setVoteTime(new Date());
            voteRecordRepository.save(record);

            
            incrementVoteCount(vote, voteType);
        }

        voteRepository.save(vote);
    }

    
    public Optional<VoteRecord> getUserVote(String voteId, String userId) {
        return voteRecordRepository.findByVoteIdAndUserId(voteId, userId);
    }

    private VoteTableResponse mapToTableResponse(Vote vote) {
        
        List<VoteRecord> records = voteRecordRepository.findByVoteId(vote.getId());
        double totalArea = records.stream()
                .filter(r -> r.getUser() != null && r.getUser().getApartmentArea() != null)
                .mapToDouble(r -> r.getUser().getApartmentArea())
                .sum();

        String result = determineResult(vote);

        return VoteTableResponse.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .endTime(vote.getEndTime())
                .status(vote.getStatus())
                .result(result)
                .forVotesCount(vote.getForVotesCount())
                .againstVotesCount(vote.getAgainstVotesCount())
                .abstentionsCount(vote.getAbstentionsCount())
                .totalVotedArea(totalArea)
                .build();
    }

    private String determineResult(Vote vote) {
        if (!"Закрыто".equals(vote.getStatus())) {
            return null;
        }
        Integer forVotes = vote.getForVotesCount() != null ? vote.getForVotesCount() : 0;
        Integer againstVotes = vote.getAgainstVotesCount() != null ? vote.getAgainstVotesCount() : 0;
        return forVotes > againstVotes ? "Принято" : "Отклонено";
    }

    private String getVoteTypeDisplay(String voteType) {
        if (voteType == null)
            return null;
        return switch (voteType) {
            case "FOR" -> "За";
            case "AGAINST" -> "Против";
            case "ABSTENTION" -> "Воздержался";
            default -> voteType;
        };
    }

    private void incrementVoteCount(Vote vote, String voteType) {
        switch (voteType) {
            case "FOR" -> vote.setForVotesCount((vote.getForVotesCount() != null ? vote.getForVotesCount() : 0) + 1);
            case "AGAINST" ->
                vote.setAgainstVotesCount((vote.getAgainstVotesCount() != null ? vote.getAgainstVotesCount() : 0) + 1);
            case "ABSTENTION" ->
                vote.setAbstentionsCount((vote.getAbstentionsCount() != null ? vote.getAbstentionsCount() : 0) + 1);
        }
    }

    private void decrementVoteCount(Vote vote, String voteType) {
        switch (voteType) {
            case "FOR" ->
                vote.setForVotesCount(Math.max(0, (vote.getForVotesCount() != null ? vote.getForVotesCount() : 0) - 1));
            case "AGAINST" -> vote.setAgainstVotesCount(
                    Math.max(0, (vote.getAgainstVotesCount() != null ? vote.getAgainstVotesCount() : 0) - 1));
            case "ABSTENTION" -> vote.setAbstentionsCount(
                    Math.max(0, (vote.getAbstentionsCount() != null ? vote.getAbstentionsCount() : 0) - 1));
        }
    }
}
