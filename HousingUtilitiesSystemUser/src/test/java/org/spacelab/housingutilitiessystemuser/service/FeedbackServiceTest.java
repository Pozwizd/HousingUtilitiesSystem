package org.spacelab.housingutilitiessystemuser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemuser.entity.FeedbackRequest;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.models.feedback.FeedbackRequestCreate;
import org.spacelab.housingutilitiessystemuser.models.feedback.FeedbackRequestResponse;
import org.spacelab.housingutilitiessystemuser.repository.FeedbackRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackService Tests")
class FeedbackServiceTest {

    @Mock
    private FeedbackRequestRepository feedbackRequestRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private User testUser;
    private FeedbackRequest testFeedback;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-id");
        testUser.setEmail("test@test.com");

        testFeedback = FeedbackRequest.builder()
                .id("feedback-id")
                .subject("Test Subject")
                .message("Test Message")
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .userId("user-id")
                .build();
    }

    @Nested
    @DisplayName("Get Requests For User")
    class GetRequestsForUser {
        @Test
        @DisplayName("Should return page of feedback responses")
        void getRequestsForUser_shouldReturnPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<FeedbackRequest> feedbackPage = new PageImpl<>(List.of(testFeedback), pageable, 1);
            when(feedbackRequestRepository.findByUserIdOrderByCreatedAtDesc(eq("user-id"), any(Pageable.class)))
                    .thenReturn(feedbackPage);

            Page<FeedbackRequestResponse> result = feedbackService.getRequestsForUser("user-id", 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getSubject()).isEqualTo("Test Subject");
        }

        @Test
        @DisplayName("Should return empty page when no feedback")
        void getRequestsForUser_shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<FeedbackRequest> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(feedbackRequestRepository.findByUserIdOrderByCreatedAtDesc(eq("user-id"), any(Pageable.class)))
                    .thenReturn(emptyPage);

            Page<FeedbackRequestResponse> result = feedbackService.getRequestsForUser("user-id", 0, 10);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Request By Id")
    class GetRequestById {
        @Test
        @DisplayName("Should return feedback when user owns it")
        void getRequestById_shouldReturnWhenUserOwns() {
            when(feedbackRequestRepository.findById("feedback-id")).thenReturn(Optional.of(testFeedback));

            Optional<FeedbackRequestResponse> result = feedbackService.getRequestById("feedback-id", "user-id");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("feedback-id");
        }

        @Test
        @DisplayName("Should return empty when user doesn't own it")
        void getRequestById_shouldReturnEmptyWhenUserDoesntOwn() {
            when(feedbackRequestRepository.findById("feedback-id")).thenReturn(Optional.of(testFeedback));

            Optional<FeedbackRequestResponse> result = feedbackService.getRequestById("feedback-id", "other-user-id");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when not found")
        void getRequestById_shouldReturnEmptyWhenNotFound() {
            when(feedbackRequestRepository.findById("unknown")).thenReturn(Optional.empty());

            Optional<FeedbackRequestResponse> result = feedbackService.getRequestById("unknown", "user-id");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Create Request")
    class CreateRequest {
        @Test
        @DisplayName("Should create feedback request")
        void createRequest_shouldCreate() {
            FeedbackRequestCreate createRequest = new FeedbackRequestCreate();
            createRequest.setSubject("New Subject");
            createRequest.setMessage("New Message");

            when(feedbackRequestRepository.save(any(FeedbackRequest.class))).thenReturn(testFeedback);

            FeedbackRequestResponse result = feedbackService.createRequest(createRequest, testUser);

            assertThat(result).isNotNull();
            verify(feedbackRequestRepository).save(any(FeedbackRequest.class));
        }
    }

    @Nested
    @DisplayName("Delete Request")
    class DeleteRequest {
        @Test
        @DisplayName("Should delete when user owns it")
        void deleteRequest_shouldDeleteWhenUserOwns() {
            when(feedbackRequestRepository.findById("feedback-id")).thenReturn(Optional.of(testFeedback));
            doNothing().when(feedbackRequestRepository).deleteById("feedback-id");

            boolean result = feedbackService.deleteRequest("feedback-id", "user-id");

            assertThat(result).isTrue();
            verify(feedbackRequestRepository).deleteById("feedback-id");
        }

        @Test
        @DisplayName("Should not delete when user doesn't own it")
        void deleteRequest_shouldNotDeleteWhenUserDoesntOwn() {
            when(feedbackRequestRepository.findById("feedback-id")).thenReturn(Optional.of(testFeedback));

            boolean result = feedbackService.deleteRequest("feedback-id", "other-user-id");

            assertThat(result).isFalse();
            verify(feedbackRequestRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should return false when not found")
        void deleteRequest_shouldReturnFalseWhenNotFound() {
            when(feedbackRequestRepository.findById("unknown")).thenReturn(Optional.empty());

            boolean result = feedbackService.deleteRequest("unknown", "user-id");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Delete Multiple Requests")
    class DeleteMultipleRequests {
        @Test
        @DisplayName("Should delete multiple requests")
        void deleteRequests_shouldDeleteMultiple() {
            when(feedbackRequestRepository.findById("feedback-id")).thenReturn(Optional.of(testFeedback));
            doNothing().when(feedbackRequestRepository).deleteById("feedback-id");

            int result = feedbackService.deleteRequests(List.of("feedback-id", "unknown"), "user-id");

            assertThat(result).isEqualTo(1);
        }
    }
}
