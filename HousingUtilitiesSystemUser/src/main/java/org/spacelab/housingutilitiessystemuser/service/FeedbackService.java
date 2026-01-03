package org.spacelab.housingutilitiessystemuser.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.entity.FeedbackRequest;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.models.feedback.FeedbackRequestCreate;
import org.spacelab.housingutilitiessystemuser.models.feedback.FeedbackRequestResponse;
import org.spacelab.housingutilitiessystemuser.repository.FeedbackRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRequestRepository feedbackRequestRepository;

    
    public Page<FeedbackRequestResponse> getRequestsForUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FeedbackRequest> feedbackPage = feedbackRequestRepository.findByUserIdOrderByCreatedAtDesc(userId,
                pageable);

        List<FeedbackRequestResponse> responses = feedbackPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, feedbackPage.getTotalElements());
    }

    
    public Optional<FeedbackRequestResponse> getRequestById(String requestId, String userId) {
        return feedbackRequestRepository.findById(requestId)
                .filter(req -> userId.equals(req.getUserId()))
                .map(this::toResponse);
    }

    
    public FeedbackRequestResponse createRequest(FeedbackRequestCreate request, User user) {
        FeedbackRequest feedbackRequest = FeedbackRequest.builder()
                .subject(request.getSubject())
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .user(user)
                .userId(user.getId())
                .build();

        FeedbackRequest saved = feedbackRequestRepository.save(feedbackRequest);
        return toResponse(saved);
    }

    
    public boolean deleteRequest(String requestId, String userId) {
        Optional<FeedbackRequest> request = feedbackRequestRepository.findById(requestId);
        if (request.isPresent() && userId.equals(request.get().getUserId())) {
            feedbackRequestRepository.deleteById(requestId);
            return true;
        }
        return false;
    }

    
    public int deleteRequests(List<String> requestIds, String userId) {
        int deleted = 0;
        for (String id : requestIds) {
            if (deleteRequest(id, userId)) {
                deleted++;
            }
        }
        return deleted;
    }

    private FeedbackRequestResponse toResponse(FeedbackRequest request) {
        return FeedbackRequestResponse.builder()
                .id(request.getId())
                .subject(request.getSubject())
                .message(request.getMessage())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
