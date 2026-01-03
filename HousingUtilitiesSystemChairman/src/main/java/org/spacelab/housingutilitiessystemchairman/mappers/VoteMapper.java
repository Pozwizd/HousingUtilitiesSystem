package org.spacelab.housingutilitiessystemchairman.mappers;

import org.mapstruct.*;
import org.spacelab.housingutilitiessystemchairman.entity.Vote;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteRequest;
import org.spacelab.housingutilitiessystemchairman.models.vote.VoteResponseTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
@Mapper(componentModel = "spring")
public interface VoteMapper {
    @Mapping(target = "result", expression = "java(calculateResult(vote))")
    VoteResponseTable toResponseTable(Vote vote);
    List<VoteResponseTable> toResponseTableList(List<Vote> votes);
    default Page<VoteResponseTable> toResponseTablePage(Page<Vote> votes) {
        if (votes == null) {
            return null;
        }
        List<VoteResponseTable> responseList = toResponseTableList(votes.getContent());
        return new PageImpl<>(responseList, votes.getPageable(), votes.getTotalElements());
    }
    default PageResponse<VoteResponseTable> toPageResponse(Page<Vote> votes) {
        if (votes == null) {
            return null;
        }
        Page<VoteResponseTable> page = toResponseTablePage(votes);
        return PageResponse.of(page);
    }
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "forVotesCount", constant = "0")
    @Mapping(target = "againstVotesCount", constant = "0")
    @Mapping(target = "abstentionsCount", constant = "0")
    Vote toEntity(VoteRequest request);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "forVotesCount", ignore = true)
    @Mapping(target = "againstVotesCount", ignore = true)
    @Mapping(target = "abstentionsCount", ignore = true)
    void partialUpdate(VoteRequest request, @MappingTarget Vote vote);
    default String calculateResult(Vote vote) {
        if (vote == null || !"Закрыто".equals(vote.getStatus())) {
            return null;
        }
        int forVotes = vote.getForVotesCount() != null ? vote.getForVotesCount() : 0;
        int againstVotes = vote.getAgainstVotesCount() != null ? vote.getAgainstVotesCount() : 0;
        return forVotes > againstVotes ? "Принято" : "Отклонено";
    }
}
