package org.spacelab.housingutilitiessystemchairman.models.filters.feedback;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FeedbackRequestTable {
    private int page = 0;
    private int size = 10;
    private String senderName;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
