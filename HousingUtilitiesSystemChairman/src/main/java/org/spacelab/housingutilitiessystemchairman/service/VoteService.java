package org.spacelab.housingutilitiessystemchairman.service;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemchairman.entity.Vote;
import org.spacelab.housingutilitiessystemchairman.entity.VoteRecord;
import org.spacelab.housingutilitiessystemchairman.mappers.VoteMapper;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.filters.vote.VoteParticipantRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.filters.vote.VoteRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteDetailResponse;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteParticipantResponse;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteRequest;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteResponseTable;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.VoteRecordRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.VoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final MongoTemplate mongoTemplate;
    private final VoteMapper voteMapper;
    public Vote save(Vote vote) {
        return voteRepository.save(vote);
    }
    public Optional<Vote> findById(String id) {
        return voteRepository.findById(id);
    }
    public List<Vote> findAll() {
        return voteRepository.findAll();
    }
    public void deleteById(String id) {
        voteRepository.deleteById(id);
    }
    public Vote update(Vote vote) {
        return voteRepository.save(vote);
    }
    public List<Vote> saveAll(List<Vote> votes) {
        return voteRepository.saveAll(votes);
    }
    public void deleteAll() {
        voteRepository.deleteAll();
    }
    public PageResponse<VoteResponseTable> getVotesTable(VoteRequestTable request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
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
        List<VoteResponseTable> responseList = voteMapper.toResponseTableList(votes);
        if (request.getResult() != null && !request.getResult().isEmpty()) {
            responseList = responseList.stream()
                    .filter(v -> request.getResult().equals(v.getResult()))
                    .collect(Collectors.toList());
        }
        Page<VoteResponseTable> page = new PageImpl<>(responseList, pageable, total);
        return PageResponse.of(page);
    }
    public Vote createVote(VoteRequest request) {
        Vote vote = voteMapper.toEntity(request);
        if (vote.getStatus() == null) {
            vote.setStatus("Активное");
        }
        return voteRepository.save(vote);
    }
    public Vote updateVote(String id, VoteRequest request) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vote not found: " + id));
        voteMapper.partialUpdate(request, vote);
        return voteRepository.save(vote);
    }
    public VoteResponseTable getVoteById(String id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vote not found: " + id));
        return voteMapper.toResponseTable(vote);
    }
    public VoteDetailResponse getVoteDetail(String id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vote not found: " + id));
        List<VoteRecord> records = voteRecordRepository.findByVoteId(id);
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
                .build();
    }
    public PageResponse<VoteParticipantResponse> getVoteParticipants(VoteParticipantRequestTable request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("vote").is(new ObjectId(request.getVoteId())));
        if (request.getVoteType() != null && !request.getVoteType().isEmpty()) {
            criteriaList.add(Criteria.where("voteType").is(request.getVoteType()));
        }
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        long total = mongoTemplate.count(query, VoteRecord.class);
        query.with(pageable);
        List<VoteRecord> records = mongoTemplate.find(query, VoteRecord.class);
        List<VoteParticipantResponse> responseList = records.stream()
                .map(this::mapToParticipantResponse)
                .filter(p -> {
                    if (request.getFullName() != null && !request.getFullName().isEmpty()) {
                        if (p.getFullName() == null
                                || !p.getFullName().toLowerCase().contains(request.getFullName().toLowerCase())) {
                            return false;
                        }
                    }
                    if (request.getApartmentNumber() != null && !request.getApartmentNumber().isEmpty()) {
                        if (p.getApartmentNumber() == null
                                || !p.getApartmentNumber().contains(request.getApartmentNumber())) {
                            return false;
                        }
                    }
                    if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                        if (p.getPhone() == null || !p.getPhone().contains(request.getPhone())) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        Page<VoteParticipantResponse> page = new PageImpl<>(responseList, pageable, total);
        return PageResponse.of(page);
    }
    private VoteParticipantResponse mapToParticipantResponse(VoteRecord record) {
        String fullName = "";
        String apartmentNumber = "";
        Double apartmentArea = null;
        String phone = "";
        if (record.getUser() != null) {
            fullName = record.getUser().getFullName();
            apartmentNumber = record.getUser().getApartmentNumber();
            apartmentArea = record.getUser().getApartmentArea();
            phone = record.getUser().getPhone();
        }
        String voteTypeDisplay = switch (record.getVoteType()) {
            case "FOR" -> "За";
            case "AGAINST" -> "Против";
            case "ABSTENTION" -> "Воздержался";
            default -> record.getVoteType();
        };
        return VoteParticipantResponse.builder()
                .id(record.getId())
                .fullName(fullName)
                .apartmentNumber(apartmentNumber)
                .apartmentArea(apartmentArea)
                .phone(phone)
                .voteType(record.getVoteType())
                .voteTypeDisplay(voteTypeDisplay)
                .voteTime(record.getVoteTime())
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
}
