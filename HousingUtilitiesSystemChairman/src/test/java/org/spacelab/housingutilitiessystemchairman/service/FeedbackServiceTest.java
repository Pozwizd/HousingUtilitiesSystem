package org.spacelab.housingutilitiessystemchairman.service;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemchairman.entity.FeedbackRequest;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.feedback.FeedbackDetailResponse;
import org.spacelab.housingutilitiessystemchairman.models.feedback.FeedbackResponseTable;
import org.spacelab.housingutilitiessystemchairman.models.filters.feedback.FeedbackRequestTable;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.FeedbackRequestRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackService Tests")
class FeedbackServiceTest {

    @Mock
    private FeedbackRequestRepository feedbackRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private FeedbackRequest testFeedback;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(new ObjectId().toHexString());
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setMiddleName("Middle");
        testUser.setApartmentNumber("101");
        testUser.setPhone("1234567890");

        testFeedback = new FeedbackRequest();
        testFeedback.setId("feedback-id");
        testFeedback.setSubject("Test Subject");
        testFeedback.setMessage("Test Message");
        testFeedback.setCreatedAt(LocalDateTime.now());
        testFeedback.setUser(testUser);
    }

    @Nested
    @DisplayName("Table Operations")
    class TableOperations {
        @Test
        @DisplayName("Should get feedback table without date filters")
        void getFeedbackTable_shouldReturnTable() {
            FeedbackRequestTable requestTable = new FeedbackRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            Page<FeedbackRequest> feedbackPage = new PageImpl<>(List.of(testFeedback));

            when(feedbackRequestRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(feedbackPage);

            PageResponse<FeedbackResponseTable> result = feedbackService.getFeedbackTable(requestTable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by date from and to")
        void getFeedbackTable_shouldFilterByDateRange() {
            FeedbackRequestTable requestTable = new FeedbackRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            requestTable.setDateFrom(LocalDate.of(2024, 1, 1));
            requestTable.setDateTo(LocalDate.of(2024, 12, 31));

            Page<FeedbackRequest> feedbackPage = new PageImpl<>(List.of(testFeedback));

            when(feedbackRequestRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(any(), any(), any(Pageable.class)))
                    .thenReturn(feedbackPage);

            PageResponse<FeedbackResponseTable> result = feedbackService.getFeedbackTable(requestTable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should filter by date from only")
        void getFeedbackTable_shouldFilterByDateFrom() {
            FeedbackRequestTable requestTable = new FeedbackRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            requestTable.setDateFrom(LocalDate.of(2024, 1, 1));

            Page<FeedbackRequest> feedbackPage = new PageImpl<>(List.of(testFeedback));

            when(feedbackRequestRepository.findByCreatedAtAfterOrderByCreatedAtDesc(any(), any(Pageable.class)))
                    .thenReturn(feedbackPage);

            PageResponse<FeedbackResponseTable> result = feedbackService.getFeedbackTable(requestTable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should filter by date to only")
        void getFeedbackTable_shouldFilterByDateTo() {
            FeedbackRequestTable requestTable = new FeedbackRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            requestTable.setDateTo(LocalDate.of(2024, 12, 31));

            Page<FeedbackRequest> feedbackPage = new PageImpl<>(List.of(testFeedback));

            when(feedbackRequestRepository.findByCreatedAtBeforeOrderByCreatedAtDesc(any(), any(Pageable.class)))
                    .thenReturn(feedbackPage);

            PageResponse<FeedbackResponseTable> result = feedbackService.getFeedbackTable(requestTable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle feedback without user")
        void getFeedbackTable_shouldHandleNullUser() {
            testFeedback.setUser(null);
            testFeedback.setUserId(null);

            FeedbackRequestTable requestTable = new FeedbackRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            Page<FeedbackRequest> feedbackPage = new PageImpl<>(List.of(testFeedback));

            when(feedbackRequestRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(feedbackPage);

            PageResponse<FeedbackResponseTable> result = feedbackService.getFeedbackTable(requestTable);

            assertThat(result.getContent().get(0).getSenderName()).isEqualTo("Неизвестный пользователь");
        }
    }

    @Nested
    @DisplayName("Get By Id")
    class GetById {
        @Test
        @DisplayName("Should get feedback by id")
        void getFeedbackById_shouldReturnDetail() {
            when(feedbackRequestRepository.findById("feedback-id")).thenReturn(Optional.of(testFeedback));

            FeedbackDetailResponse result = feedbackService.getFeedbackById("feedback-id");

            assertThat(result).isNotNull();
            assertThat(result.getSubject()).isEqualTo("Test Subject");
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getFeedbackById_shouldThrowException() {
            when(feedbackRequestRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> feedbackService.getFeedbackById("unknown"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {
        @Test
        @DisplayName("Should delete feedback")
        void deleteFeedback_shouldDelete() {
            doNothing().when(feedbackRequestRepository).deleteById("feedback-id");

            feedbackService.deleteFeedback("feedback-id");

            verify(feedbackRequestRepository).deleteById("feedback-id");
        }
    }
}
