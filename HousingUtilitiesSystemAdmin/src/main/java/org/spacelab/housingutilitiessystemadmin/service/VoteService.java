package org.spacelab.housingutilitiessystemadmin.service;

import lombok.AllArgsConstructor;
import org.spacelab.housingutilitiessystemadmin.entity.Vote;
import org.spacelab.housingutilitiessystemadmin.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;

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
}