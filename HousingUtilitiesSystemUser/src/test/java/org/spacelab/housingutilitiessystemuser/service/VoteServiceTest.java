package org.spacelab.housingutilitiessystemuser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService Tests")
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private VoteRecordRepository voteRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private VoteService voteService;

    private User testUser;
    private Vote testVote;
    private VoteRecord testVoteRecord;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-id");
        testUser.setEmail("test@test.com");
        testUser.setApartmentArea(50.0);

        testVote = new Vote();
        testVote.setId("vote-id");
        testVote.setTitle("Test Vote");
        testVote.setStatus("Активное");
        testVote.setForVotesCount(5);
        testVote.setAgainstVotesCount(3);
        testVote.setAbstentionsCount(1);

        testVoteRecord = new VoteRecord();
        testVoteRecord.setId("record-id");
        testVoteRecord.setVote(testVote);
        testVoteRecord.setUser(testUser);
        testVoteRecord.setVoteType("FOR");
    }

    @Nested
    @DisplayName("Get Votes Table")
    class GetVotesTable {
        @Test
        @DisplayName("Should return paginated votes with null page/size")
        void getVotesTable_shouldHandleNullPageSize() {
            VoteTableRequest request = new VoteTableRequest();
            // page and size are null - should default to 0 and 10

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());

            PageResponse<VoteTableResponse> result = voteService.getVotesTable(request);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by title")
        void getVotesTable_shouldFilterByTitle() {
            VoteTableRequest request = new VoteTableRequest();
            request.setPage(0);
            request.setSize(10);
            request.setTitle("Test");

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());

            PageResponse<VoteTableResponse> result = voteService.getVotesTable(request);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by status")
        void getVotesTable_shouldFilterByStatus() {
            VoteTableRequest request = new VoteTableRequest();
            request.setPage(0);
            request.setSize(10);
            request.setStatus("Активное");

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());

            PageResponse<VoteTableResponse> result = voteService.getVotesTable(request);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by result - Принято")
        void getVotesTable_shouldFilterByResultAccepted() {
            testVote.setStatus("Закрыто");
            testVote.setForVotesCount(10);
            testVote.setAgainstVotesCount(5);
            
            VoteTableRequest request = new VoteTableRequest();
            request.setPage(0);
            request.setSize(10);
            request.setResult("Принято");

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());

            PageResponse<VoteTableResponse> result = voteService.getVotesTable(request);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by result - Отклонено")
        void getVotesTable_shouldFilterByResultRejected() {
            testVote.setStatus("Закрыто");
            testVote.setForVotesCount(3);
            testVote.setAgainstVotesCount(10);
            
            VoteTableRequest request = new VoteTableRequest();
            request.setPage(0);
            request.setSize(10);
            request.setResult("Отклонено");

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());

            PageResponse<VoteTableResponse> result = voteService.getVotesTable(request);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should calculate total area from records")
        void getVotesTable_shouldCalculateTotalArea() {
            VoteTableRequest request = new VoteTableRequest();
            request.setPage(0);
            request.setSize(10);

            VoteRecord recordWithArea = new VoteRecord();
            recordWithArea.setUser(testUser);

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(List.of(recordWithArea));

            PageResponse<VoteTableResponse> result = voteService.getVotesTable(request);

            assertThat(result.getContent().get(0).getTotalVotedArea()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should handle records with null user or null area")
        void getVotesTable_shouldHandleNullUserOrArea() {
            VoteTableRequest request = new VoteTableRequest();
            request.setPage(0);
            request.setSize(10);

            VoteRecord recordNullUser = new VoteRecord();
            recordNullUser.setUser(null);

            User userNullArea = new User();
            userNullArea.setApartmentArea(null);
            VoteRecord recordNullArea = new VoteRecord();
            recordNullArea.setUser(userNullArea);

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(List.of(recordNullUser, recordNullArea));

            PageResponse<VoteTableResponse> result = voteService.getVotesTable(request);

            assertThat(result.getContent().get(0).getTotalVotedArea()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should filter out non-matching result")
        void getVotesTable_shouldFilterOutNonMatchingResult() {
            testVote.setStatus("Закрыто");
            testVote.setForVotesCount(10);
            testVote.setAgainstVotesCount(5);
            
            VoteTableRequest request = new VoteTableRequest();
            request.setPage(0);
            request.setSize(10);
            request.setResult("Отклонено"); // Vote is Принято, should be filtered out

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());

            PageResponse<VoteTableResponse> result = voteService.getVotesTable(request);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null vote counts in determineResult")
        void getVotesTable_shouldHandleNullVoteCounts() {
            testVote.setStatus("Закрыто");
            testVote.setForVotesCount(null);
            testVote.setAgainstVotesCount(null);
            
            VoteTableRequest request = new VoteTableRequest();
            request.setPage(0);
            request.setSize(10);

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());

            PageResponse<VoteTableResponse> result = voteService.getVotesTable(request);

            assertThat(result.getContent().get(0).getResult()).isEqualTo("Отклонено"); // 0 vs 0 => rejected
        }
    }

    @Nested
    @DisplayName("Get Vote Detail")
    class GetVoteDetail {
        @Test
        @DisplayName("Should return vote detail with user vote FOR")
        void getVoteDetail_shouldReturnDetailWithForVote() {
            testVoteRecord.setVoteType("FOR");
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(List.of(testVoteRecord));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.of(testVoteRecord));

            VoteDetailResponse result = voteService.getVoteDetail("vote-id", "user-id");

            assertThat(result.isUserHasVoted()).isTrue();
            assertThat(result.getUserVoteType()).isEqualTo("FOR");
            assertThat(result.getUserVoteTypeDisplay()).isEqualTo("За");
            assertThat(result.getForVotesArea()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should return vote detail with user vote AGAINST")
        void getVoteDetail_shouldReturnDetailWithAgainstVote() {
            testVoteRecord.setVoteType("AGAINST");
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(List.of(testVoteRecord));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.of(testVoteRecord));

            VoteDetailResponse result = voteService.getVoteDetail("vote-id", "user-id");

            assertThat(result.getUserVoteTypeDisplay()).isEqualTo("Против");
            assertThat(result.getAgainstVotesArea()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should return vote detail with user vote ABSTENTION")
        void getVoteDetail_shouldReturnDetailWithAbstentionVote() {
            testVoteRecord.setVoteType("ABSTENTION");
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(List.of(testVoteRecord));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.of(testVoteRecord));

            VoteDetailResponse result = voteService.getVoteDetail("vote-id", "user-id");

            assertThat(result.getUserVoteTypeDisplay()).isEqualTo("Воздержался");
            assertThat(result.getAbstentionsArea()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should handle unknown vote type")
        void getVoteDetail_shouldHandleUnknownVoteType() {
            testVoteRecord.setVoteType("UNKNOWN");
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(List.of(testVoteRecord));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.of(testVoteRecord));

            VoteDetailResponse result = voteService.getVoteDetail("vote-id", "user-id");

            assertThat(result.getUserVoteTypeDisplay()).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("Should return detail when user has not voted")
        void getVoteDetail_whenUserHasNotVoted() {
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.empty());

            VoteDetailResponse result = voteService.getVoteDetail("vote-id", "user-id");

            assertThat(result.isUserHasVoted()).isFalse();
            assertThat(result.getUserVoteType()).isNull();
            assertThat(result.getUserVoteTypeDisplay()).isNull();
        }

        @Test
        @DisplayName("Should calculate result for closed vote - Принято")
        void getVoteDetail_shouldCalculateResultAccepted() {
            testVote.setStatus("Закрыто");
            testVote.setForVotesCount(10);
            testVote.setAgainstVotesCount(5);
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.empty());

            VoteDetailResponse result = voteService.getVoteDetail("vote-id", "user-id");

            assertThat(result.getResult()).isEqualTo("Принято");
        }

        @Test
        @DisplayName("Should calculate result for closed vote - Отклонено")
        void getVoteDetail_shouldCalculateResultRejected() {
            testVote.setStatus("Закрыто");
            testVote.setForVotesCount(5);
            testVote.setAgainstVotesCount(10);
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(Collections.emptyList());
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.empty());

            VoteDetailResponse result = voteService.getVoteDetail("vote-id", "user-id");

            assertThat(result.getResult()).isEqualTo("Отклонено");
        }

        @Test
        @DisplayName("Should handle record with null user")
        void getVoteDetail_shouldHandleRecordWithNullUser() {
            VoteRecord recordNullUser = new VoteRecord();
            recordNullUser.setVoteType("FOR");
            recordNullUser.setUser(null);
            
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("vote-id")).thenReturn(List.of(recordNullUser));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.empty());

            VoteDetailResponse result = voteService.getVoteDetail("vote-id", "user-id");

            assertThat(result.getForVotesArea()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should throw exception when vote not found")
        void getVoteDetail_shouldThrowWhenNotFound() {
            when(voteRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> voteService.getVoteDetail("unknown", "user-id"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Cast Vote")
    class CastVote {
        @Test
        @DisplayName("Should cast new vote FOR")
        void castVote_shouldCastNewVoteFor() {
            testVote.setForVotesCount(null); // Test null handling
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.empty());

            voteService.castVote("vote-id", "user-id", "FOR");

            assertThat(testVote.getForVotesCount()).isEqualTo(1);
            verify(voteRecordRepository).save(any(VoteRecord.class));
            verify(voteRepository).save(testVote);
        }

        @Test
        @DisplayName("Should cast new vote AGAINST")
        void castVote_shouldCastNewVoteAgainst() {
            testVote.setAgainstVotesCount(null);
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.empty());

            voteService.castVote("vote-id", "user-id", "AGAINST");

            assertThat(testVote.getAgainstVotesCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should cast new vote ABSTENTION")
        void castVote_shouldCastNewVoteAbstention() {
            testVote.setAbstentionsCount(null);
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.empty());

            voteService.castVote("vote-id", "user-id", "ABSTENTION");

            assertThat(testVote.getAbstentionsCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should change vote from FOR to AGAINST")
        void castVote_shouldChangeFromForToAgainst() {
            testVoteRecord.setVoteType("FOR");
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.of(testVoteRecord));

            int originalFor = testVote.getForVotesCount();
            int originalAgainst = testVote.getAgainstVotesCount();

            voteService.castVote("vote-id", "user-id", "AGAINST");

            assertThat(testVote.getForVotesCount()).isEqualTo(originalFor - 1);
            assertThat(testVote.getAgainstVotesCount()).isEqualTo(originalAgainst + 1);
            assertThat(testVoteRecord.getVoteType()).isEqualTo("AGAINST");
        }

        @Test
        @DisplayName("Should change vote from AGAINST to ABSTENTION")
        void castVote_shouldChangeFromAgainstToAbstention() {
            testVoteRecord.setVoteType("AGAINST");
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.of(testVoteRecord));

            int originalAgainst = testVote.getAgainstVotesCount();
            int originalAbstention = testVote.getAbstentionsCount();

            voteService.castVote("vote-id", "user-id", "ABSTENTION");

            assertThat(testVote.getAgainstVotesCount()).isEqualTo(originalAgainst - 1);
            assertThat(testVote.getAbstentionsCount()).isEqualTo(originalAbstention + 1);
        }

        @Test
        @DisplayName("Should change vote from ABSTENTION to FOR")
        void castVote_shouldChangeFromAbstentionToFor() {
            testVoteRecord.setVoteType("ABSTENTION");
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.of(testVoteRecord));

            int originalAbstention = testVote.getAbstentionsCount();
            int originalFor = testVote.getForVotesCount();

            voteService.castVote("vote-id", "user-id", "FOR");

            assertThat(testVote.getAbstentionsCount()).isEqualTo(originalAbstention - 1);
            assertThat(testVote.getForVotesCount()).isEqualTo(originalFor + 1);
        }

        @Test
        @DisplayName("Should throw when voting is closed")
        void castVote_shouldThrowWhenClosed() {
            testVote.setStatus("Закрыто");
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));

            assertThatThrownBy(() -> voteService.castVote("vote-id", "user-id", "FOR"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("закрыто");
        }

        @Test
        @DisplayName("Should throw when vote not found")
        void castVote_shouldThrowWhenVoteNotFound() {
            when(voteRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> voteService.castVote("unknown", "user-id", "FOR"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void castVote_shouldThrowWhenUserNotFound() {
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(userRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> voteService.castVote("vote-id", "unknown", "FOR"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle decrementing null counts")
        void castVote_shouldHandleDecrementingNullCounts() {
            testVote.setForVotesCount(null);
            testVoteRecord.setVoteType("FOR");
            when(voteRepository.findById("vote-id")).thenReturn(Optional.of(testVote));
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.of(testVoteRecord));

            voteService.castVote("vote-id", "user-id", "AGAINST");

            assertThat(testVote.getForVotesCount()).isEqualTo(0); // Max(0, null-1) = 0
        }
    }

    @Nested
    @DisplayName("Get User Vote")
    class GetUserVote {
        @Test
        @DisplayName("Should return user vote")
        void getUserVote_shouldReturnVote() {
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.of(testVoteRecord));

            Optional<VoteRecord> result = voteService.getUserVote("vote-id", "user-id");

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when no vote")
        void getUserVote_shouldReturnEmpty() {
            when(voteRecordRepository.findByVoteIdAndUserId("vote-id", "user-id"))
                    .thenReturn(Optional.empty());

            Optional<VoteRecord> result = voteService.getUserVote("vote-id", "user-id");

            assertThat(result).isEmpty();
        }
    }
}
