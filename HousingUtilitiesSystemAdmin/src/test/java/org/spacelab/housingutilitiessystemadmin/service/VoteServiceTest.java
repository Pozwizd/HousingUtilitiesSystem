package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.Vote;
import org.spacelab.housingutilitiessystemadmin.repository.VoteRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService Tests")
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @InjectMocks
    private VoteService voteService;

    private Vote testVote;

    @BeforeEach
    void setUp() {
        testVote = new Vote();
        testVote.setId("vote-id-123");
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {
        @Test
        @DisplayName("Should save and return vote")
        void save_shouldSaveAndReturnVote() {
            when(voteRepository.save(any(Vote.class))).thenReturn(testVote);

            Vote result = voteService.save(testVote);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("vote-id-123");
            verify(voteRepository).save(testVote);
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {
        @Test
        @DisplayName("Should return Optional with vote when found")
        void findById_shouldReturnOptionalWithVote() {
            when(voteRepository.findById("vote-id-123")).thenReturn(Optional.of(testVote));

            Optional<Vote> result = voteService.findById("vote-id-123");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("vote-id-123");
        }

        @Test
        @DisplayName("Should return empty Optional when not found")
        void findById_shouldReturnEmptyOptional() {
            when(voteRepository.findById("nonexistent")).thenReturn(Optional.empty());

            Optional<Vote> result = voteService.findById("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {
        @Test
        @DisplayName("Should return all votes")
        void findAll_shouldReturnAllVotes() {
            Vote vote2 = new Vote();
            vote2.setId("vote-id-456");
            List<Vote> votes = Arrays.asList(testVote, vote2);
            when(voteRepository.findAll()).thenReturn(votes);

            List<Vote> result = voteService.findAll();

            assertThat(result).hasSize(2);
            verify(voteRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no votes")
        void findAll_shouldReturnEmptyList() {
            when(voteRepository.findAll()).thenReturn(List.of());

            List<Vote> result = voteService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {
        @Test
        @DisplayName("Should delete vote by id")
        void deleteById_shouldDeleteVote() {
            doNothing().when(voteRepository).deleteById("vote-id-123");

            voteService.deleteById("vote-id-123");

            verify(voteRepository).deleteById("vote-id-123");
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update and return vote")
        void update_shouldUpdateAndReturnVote() {
            when(voteRepository.save(any(Vote.class))).thenReturn(testVote);

            Vote result = voteService.update(testVote);

            assertThat(result).isNotNull();
            verify(voteRepository).save(testVote);
        }
    }

    @Nested
    @DisplayName("saveAll Tests")
    class SaveAllTests {
        @Test
        @DisplayName("Should save all votes")
        void saveAll_shouldSaveAllVotes() {
            Vote vote2 = new Vote();
            vote2.setId("vote-id-456");
            List<Vote> votes = Arrays.asList(testVote, vote2);
            when(voteRepository.saveAll(votes)).thenReturn(votes);

            List<Vote> result = voteService.saveAll(votes);

            assertThat(result).hasSize(2);
            verify(voteRepository).saveAll(votes);
        }
    }

    @Nested
    @DisplayName("deleteAll Tests")
    class DeleteAllTests {
        @Test
        @DisplayName("Should delete all votes")
        void deleteAll_shouldDeleteAllVotes() {
            doNothing().when(voteRepository).deleteAll();

            voteService.deleteAll();

            verify(voteRepository).deleteAll();
        }
    }
}
