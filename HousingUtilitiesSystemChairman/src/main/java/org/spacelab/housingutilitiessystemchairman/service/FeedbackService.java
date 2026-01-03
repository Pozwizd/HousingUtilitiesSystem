package org.spacelab.housingutilitiessystemchairman.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemchairman.entity.FeedbackRequest;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.feedback.FeedbackDetailResponse;
import org.spacelab.housingutilitiessystemchairman.models.feedback.FeedbackResponseTable;
import org.spacelab.housingutilitiessystemchairman.models.filters.feedback.FeedbackRequestTable;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.FeedbackRequestRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {
    private final FeedbackRequestRepository feedbackRequestRepository;
    private final UserRepository userRepository;

    public PageResponse<FeedbackResponseTable> getFeedbackTable(FeedbackRequestTable request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FeedbackRequest> feedbackPage;
        LocalDate dateFrom = request.getDateFrom();
        LocalDate dateTo = request.getDateTo();
        if (dateFrom != null && dateTo != null) {
            feedbackPage = feedbackRequestRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                    dateFrom.atStartOfDay(),
                    dateTo.atTime(LocalTime.MAX),
                    pageable);
        } else if (dateFrom != null) {
            feedbackPage = feedbackRequestRepository.findByCreatedAtAfterOrderByCreatedAtDesc(
                    dateFrom.atStartOfDay(),
                    pageable);
        } else if (dateTo != null) {
            feedbackPage = feedbackRequestRepository.findByCreatedAtBeforeOrderByCreatedAtDesc(
                    dateTo.atTime(LocalTime.MAX),
                    pageable);
        } else {
            feedbackPage = feedbackRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        Page<FeedbackResponseTable> responsePage = feedbackPage.map(this::mapToTableResponse);
        return PageResponse.of(responsePage);
    }

    public FeedbackDetailResponse getFeedbackById(String id) {
        FeedbackRequest feedback = feedbackRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback request not found: " + id));
        return mapToDetailResponse(feedback);
    }

    public void deleteFeedback(String id) {
        feedbackRequestRepository.deleteById(id);
        log.info("Deleted feedback request: {}", id);
    }

    private FeedbackResponseTable mapToTableResponse(FeedbackRequest feedback) {
        User user = getUser(feedback);
        return FeedbackResponseTable.builder()
                .id(feedback.getId())
                .senderName(user != null ? user.getFullName() : "Неизвестный пользователь")
                .apartmentNumber(user != null ? user.getApartmentNumber() : "-")
                .phone(user != null ? user.getPhone() : "-")
                .subject(feedback.getSubject())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private FeedbackDetailResponse mapToDetailResponse(FeedbackRequest feedback) {
        User user = getUser(feedback);
        return FeedbackDetailResponse.builder()
                .id(feedback.getId())
                .subject(feedback.getSubject())
                .message(feedback.getMessage())
                .createdAt(feedback.getCreatedAt())
                .senderName(user != null ? user.getFullName() : "Неизвестный пользователь")
                .apartmentNumber(user != null ? user.getApartmentNumber() : "-")
                .phone(user != null ? user.getPhone() : "-")
                .build();
    }

    private User getUser(FeedbackRequest feedback) {
        if (feedback.getUser() != null) {
            return feedback.getUser();
        }
        if (feedback.getUserId() != null && ObjectId.isValid(feedback.getUserId())) {
            return userRepository.findById(new ObjectId(feedback.getUserId())).orElse(null);
        }
        return null;
    }
}
