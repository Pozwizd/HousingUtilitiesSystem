package org.spacelab.housingutilitiessystemchairman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.Vote;
import org.spacelab.housingutilitiessystemchairman.entity.VoteRecord;
import org.spacelab.housingutilitiessystemchairman.mappers.VoteMapper;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.filters.vote.VoteParticipantRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.filters.vote.VoteRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.vote.*;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.VoteRecordRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.VoteRepository;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService Tests")
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private VoteRecordRepository voteRecordRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private VoteMapper voteMapper;

    @InjectMocks
    private VoteService voteService;

    private Vote testVote;
    private VoteRequest testVoteRequest;
    private VoteResponseTable testVoteResponseTable;

    @BeforeEach
    void setUp() {
        testVote = new Vote();
        testVote.setId("507f1f77bcf86cd799439020");
        testVote.setTitle("Test Vote");
        testVote.setForVotesCount(10);
        testVote.setAgainstVotesCount(5);
        testVote.setAbstentionsCount(2);
        testVote.setStatus("Активное");

        testVoteRequest = new VoteRequest();
        testVoteRequest.setTitle("Test Vote");

        testVoteResponseTable = new VoteResponseTable();
        testVoteResponseTable.setId("507f1f77bcf86cd799439020");
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        @Test
        @DisplayName("Should save vote")
        void save_shouldSaveVote() {
            when(voteRepository.save(any(Vote.class))).thenReturn(testVote);
            Vote result = voteService.save(testVote);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnVote() {
            when(voteRepository.findById("507f1f77bcf86cd799439020")).thenReturn(Optional.of(testVote));
            Optional<Vote> result = voteService.findById("507f1f77bcf86cd799439020");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when not found")
        void findById_shouldReturnEmpty() {
            when(voteRepository.findById("unknown")).thenReturn(Optional.empty());
            Optional<Vote> result = voteService.findById("unknown");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all votes")
        void findAll_shouldReturnAllVotes() {
            when(voteRepository.findAll()).thenReturn(Arrays.asList(testVote, new Vote()));
            List<Vote> result = voteService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete by id")
        void deleteById_shouldDeleteVote() {
            doNothing().when(voteRepository).deleteById("507f1f77bcf86cd799439020");
            voteService.deleteById("507f1f77bcf86cd799439020");
            verify(voteRepository).deleteById("507f1f77bcf86cd799439020");
        }

        @Test
        @DisplayName("Should update vote")
        void update_shouldUpdateVote() {
            when(voteRepository.save(any(Vote.class))).thenReturn(testVote);
            Vote result = voteService.update(testVote);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should save all votes")
        void saveAll_shouldSaveAllVotes() {
            List<Vote> votes = Arrays.asList(testVote, new Vote());
            when(voteRepository.saveAll(votes)).thenReturn(votes);
            List<Vote> result = voteService.saveAll(votes);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllVotes() {
            doNothing().when(voteRepository).deleteAll();
            voteService.deleteAll();
            verify(voteRepository).deleteAll();
        }
    }

    @Nested
    @DisplayName("Table Operations")
    class TableOperations {
        @Test
        @DisplayName("Should get votes table")
        void getVotesTable_shouldReturnTable() {
            VoteRequestTable requestTable = new VoteRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteMapper.toResponseTableList(any())).thenReturn(List.of(testVoteResponseTable));

            PageResponse<VoteResponseTable> result = voteService.getVotesTable(requestTable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by title")
        void getVotesTable_shouldFilterByTitle() {
            VoteRequestTable requestTable = new VoteRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            requestTable.setTitle("Test");

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteMapper.toResponseTableList(any())).thenReturn(List.of(testVoteResponseTable));

            PageResponse<VoteResponseTable> result = voteService.getVotesTable(requestTable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should filter by status")
        void getVotesTable_shouldFilterByStatus() {
            VoteRequestTable requestTable = new VoteRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            requestTable.setStatus("Активное");

            when(mongoTemplate.count(any(), eq(Vote.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(Vote.class))).thenReturn(List.of(testVote));
            when(voteMapper.toResponseTableList(any())).thenReturn(List.of(testVoteResponseTable));

            PageResponse<VoteResponseTable> result = voteService.getVotesTable(requestTable);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Create Vote")
    class CreateVote {
        @Test
        @DisplayName("Should create vote")
        void createVote_shouldCreate() {
            when(voteMapper.toEntity(testVoteRequest)).thenReturn(testVote);
            when(voteRepository.save(any(Vote.class))).thenReturn(testVote);

            Vote result = voteService.createVote(testVoteRequest);

            assertThat(result).isNotNull();
            verify(voteRepository).save(any(Vote.class));
        }

        @Test
        @DisplayName("Should set default status if null")
        void createVote_shouldSetDefaultStatus() {
            Vote voteWithoutStatus = new Vote();
            voteWithoutStatus.setTitle("No Status");
            when(voteMapper.toEntity(testVoteRequest)).thenReturn(voteWithoutStatus);
            when(voteRepository.save(any(Vote.class))).thenAnswer(inv -> inv.getArgument(0));

            Vote result = voteService.createVote(testVoteRequest);

            assertThat(result.getStatus()).isEqualTo("Активное");
        }
    }

    @Nested
    @DisplayName("Update Vote")
    class UpdateVote {
        @Test
        @DisplayName("Should update vote")
        void updateVote_shouldUpdate() {
            when(voteRepository.findById("507f1f77bcf86cd799439020")).thenReturn(Optional.of(testVote));
            when(voteRepository.save(any(Vote.class))).thenReturn(testVote);

            Vote result = voteService.updateVote("507f1f77bcf86cd799439020", testVoteRequest);

            assertThat(result).isNotNull();
            verify(voteMapper).partialUpdate(testVoteRequest, testVote);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void updateVote_shouldThrowException_whenNotFound() {
            when(voteRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> voteService.updateVote("unknown", testVoteRequest))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Get Vote By Id")
    class GetVoteById {
        @Test
        @DisplayName("Should get vote by id")
        void getVoteById_shouldReturnVote() {
            when(voteRepository.findById("507f1f77bcf86cd799439020")).thenReturn(Optional.of(testVote));
            when(voteMapper.toResponseTable(testVote)).thenReturn(testVoteResponseTable);

            VoteResponseTable result = voteService.getVoteById("507f1f77bcf86cd799439020");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getVoteById_shouldThrowException_whenNotFound() {
            when(voteRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> voteService.getVoteById("unknown"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Vote Detail")
    class VoteDetail {
        @Test
        @DisplayName("Should get vote detail")
        void getVoteDetail_shouldReturnDetail() {
            when(voteRepository.findById("507f1f77bcf86cd799439020")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("507f1f77bcf86cd799439020")).thenReturn(Collections.emptyList());

            VoteDetailResponse result = voteService.getVoteDetail("507f1f77bcf86cd799439020");

            assertThat(result).isNotNull();
            assertThat(result.getForVotesCount()).isEqualTo(10);
            assertThat(result.getAgainstVotesCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getVoteDetail_shouldThrowException_whenNotFound() {
            when(voteRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> voteService.getVoteDetail("unknown"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should return result Принято when forVotes > againstVotes and status is Закрыто")
        void getVoteDetail_shouldReturnResultPrinyato() {
            testVote.setStatus("Закрыто");
            testVote.setForVotesCount(10);
            testVote.setAgainstVotesCount(5);
            when(voteRepository.findById("507f1f77bcf86cd799439020")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("507f1f77bcf86cd799439020")).thenReturn(Collections.emptyList());

            VoteDetailResponse result = voteService.getVoteDetail("507f1f77bcf86cd799439020");

            assertThat(result.getResult()).isEqualTo("Принято");
        }

        @Test
        @DisplayName("Should return result Отклонено when againstVotes >= forVotes")
        void getVoteDetail_shouldReturnResultOtkloneno() {
            testVote.setStatus("Закрыто");
            testVote.setForVotesCount(5);
            testVote.setAgainstVotesCount(10);
            when(voteRepository.findById("507f1f77bcf86cd799439020")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("507f1f77bcf86cd799439020")).thenReturn(Collections.emptyList());

            VoteDetailResponse result = voteService.getVoteDetail("507f1f77bcf86cd799439020");

            assertThat(result.getResult()).isEqualTo("Отклонено");
        }

        @Test
        @DisplayName("Should return null result when vote is not closed")
        void getVoteDetail_shouldReturnNullResultWhenNotClosed() {
            testVote.setStatus("Активное");
            when(voteRepository.findById("507f1f77bcf86cd799439020")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("507f1f77bcf86cd799439020")).thenReturn(Collections.emptyList());

            VoteDetailResponse result = voteService.getVoteDetail("507f1f77bcf86cd799439020");

            assertThat(result.getResult()).isNull();
        }

        @Test
        @DisplayName("Should handle null vote counts")
        void getVoteDetail_shouldHandleNullCounts() {
            testVote.setStatus("Закрыто");
            testVote.setForVotesCount(null);
            testVote.setAgainstVotesCount(null);
            when(voteRepository.findById("507f1f77bcf86cd799439020")).thenReturn(Optional.of(testVote));
            when(voteRecordRepository.findByVoteId("507f1f77bcf86cd799439020")).thenReturn(Collections.emptyList());

            VoteDetailResponse result = voteService.getVoteDetail("507f1f77bcf86cd799439020");

            // With null counts, 0 vs 0, so Отклонено (not >)
            assertThat(result.getResult()).isEqualTo("Отклонено");
        }
    }

    @Nested
    @DisplayName("Vote Participants")
    class VoteParticipants {
        @Test
        @DisplayName("Should get vote participants")
        void getVoteParticipants_shouldReturnParticipants() {
            VoteParticipantRequestTable request = new VoteParticipantRequestTable();
            request.setVoteId("507f1f77bcf86cd799439020");
            request.setPage(0);
            request.setSize(10);

            VoteRecord record = new VoteRecord();
            record.setId("record-id");
            record.setVoteType("FOR");

            when(mongoTemplate.count(any(), eq(VoteRecord.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(VoteRecord.class))).thenReturn(List.of(record));

            PageResponse<VoteParticipantResponse> result = voteService.getVoteParticipants(request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should filter by vote type")
        void getVoteParticipants_shouldFilterByVoteType() {
            VoteParticipantRequestTable request = new VoteParticipantRequestTable();
            request.setVoteId("507f1f77bcf86cd799439020");
            request.setPage(0);
            request.setSize(10);
            request.setVoteType("FOR");

            VoteRecord record = new VoteRecord();
            record.setId("record-id");
            record.setVoteType("FOR");

            when(mongoTemplate.count(any(), eq(VoteRecord.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(VoteRecord.class))).thenReturn(List.of(record));

            PageResponse<VoteParticipantResponse> result = voteService.getVoteParticipants(request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should filter by fullName")
        void getVoteParticipants_shouldFilterByFullName() {
            VoteParticipantRequestTable request = new VoteParticipantRequestTable();
            request.setVoteId("507f1f77bcf86cd799439020");
            request.setPage(0);
            request.setSize(10);
            request.setFullName("John");

            org.spacelab.housingutilitiessystemchairman.entity.User user = 
                new org.spacelab.housingutilitiessystemchairman.entity.User();
            user.setFirstName("John");
            user.setMiddleName("M");
            user.setLastName("Doe");
            user.setApartmentNumber("101");
            user.setPhone("123");

            VoteRecord record = new VoteRecord();
            record.setId("record-id");
            record.setVoteType("FOR");
            record.setUser(user);

            when(mongoTemplate.count(any(), eq(VoteRecord.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(VoteRecord.class))).thenReturn(List.of(record));

            PageResponse<VoteParticipantResponse> result = voteService.getVoteParticipants(request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should filter by apartment number")
        void getVoteParticipants_shouldFilterByApartmentNumber() {
            VoteParticipantRequestTable request = new VoteParticipantRequestTable();
            request.setVoteId("507f1f77bcf86cd799439020");
            request.setPage(0);
            request.setSize(10);
            request.setApartmentNumber("101");

            org.spacelab.housingutilitiessystemchairman.entity.User user = 
                new org.spacelab.housingutilitiessystemchairman.entity.User();
            user.setFirstName("John");
            user.setApartmentNumber("101");

            VoteRecord record = new VoteRecord();
            record.setId("record-id");
            record.setVoteType("AGAINST");
            record.setUser(user);

            when(mongoTemplate.count(any(), eq(VoteRecord.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(VoteRecord.class))).thenReturn(List.of(record));

            PageResponse<VoteParticipantResponse> result = voteService.getVoteParticipants(request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should filter by phone")
        void getVoteParticipants_shouldFilterByPhone() {
            VoteParticipantRequestTable request = new VoteParticipantRequestTable();
            request.setVoteId("507f1f77bcf86cd799439020");
            request.setPage(0);
            request.setSize(10);
            request.setPhone("123");

            org.spacelab.housingutilitiessystemchairman.entity.User user = 
                new org.spacelab.housingutilitiessystemchairman.entity.User();
            user.setPhone("1234567890");

            VoteRecord record = new VoteRecord();
            record.setId("record-id");
            record.setVoteType("ABSTENTION");
            record.setUser(user);

            when(mongoTemplate.count(any(), eq(VoteRecord.class))).thenReturn(1L);
            when(mongoTemplate.find(any(), eq(VoteRecord.class))).thenReturn(List.of(record));

            PageResponse<VoteParticipantResponse> result = voteService.getVoteParticipants(request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should map all vote types correctly")
        void getVoteParticipants_shouldMapVoteTypes() {
            VoteParticipantRequestTable request = new VoteParticipantRequestTable();
            request.setVoteId("507f1f77bcf86cd799439020");
            request.setPage(0);
            request.setSize(10);

            VoteRecord forRecord = new VoteRecord();
            forRecord.setId("for-id");
            forRecord.setVoteType("FOR");

            VoteRecord againstRecord = new VoteRecord();
            againstRecord.setId("against-id");
            againstRecord.setVoteType("AGAINST");

            VoteRecord abstentionRecord = new VoteRecord();
            abstentionRecord.setId("abstention-id");
            abstentionRecord.setVoteType("ABSTENTION");

            VoteRecord otherRecord = new VoteRecord();
            otherRecord.setId("other-id");
            otherRecord.setVoteType("OTHER");

            when(mongoTemplate.count(any(), eq(VoteRecord.class))).thenReturn(4L);
            when(mongoTemplate.find(any(), eq(VoteRecord.class)))
                .thenReturn(List.of(forRecord, againstRecord, abstentionRecord, otherRecord));

            PageResponse<VoteParticipantResponse> result = voteService.getVoteParticipants(request);

            assertThat(result.getContent()).hasSize(4);
        }
    }
}
