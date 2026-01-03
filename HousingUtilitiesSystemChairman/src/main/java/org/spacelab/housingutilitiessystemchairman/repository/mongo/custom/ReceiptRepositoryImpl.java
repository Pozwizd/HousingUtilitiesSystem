package org.spacelab.housingutilitiessystemchairman.repository.mongo.custom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Receipt;
import org.spacelab.housingutilitiessystemchairman.models.filters.receipt.ReceiptRequestTable;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
@Repository
@RequiredArgsConstructor
@Slf4j
public class ReceiptRepositoryImpl implements ReceiptRepositoryCustom {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final MongoTemplate mongoTemplate;
    @Override
    public Page<Receipt> findReceiptsWithFilters(ReceiptRequestTable filter) {
        log.debug("Building aggregation for receipts with filters: {}", filter);
        int page = Math.max(filter.getPage(), 0);
        int size = filter.getSize() <= 0 ? 10 : filter.getSize();
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "localDate")
        );
        long totalCount = countReceiptsWithFilters(filter);
        List<Receipt> receipts = findReceiptsWithFiltersInternal(filter, pageable);
        return new PageImpl<>(receipts, pageable, totalCount);
    }
    private List<Receipt> findReceiptsWithFiltersInternal(ReceiptRequestTable filter, Pageable pageable) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (!preLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0]))));
        }
        operations.add(sort(pageable.getSort()));
        operations.add(skip((long) pageable.getPageNumber() * pageable.getPageSize()));
        operations.add(limit(pageable.getPageSize()));
        if (needsBillLookup(filter)) {
            operations.add(lookup("bill", "bill", "_id", "billData"));
            operations.add(unwind("billData", true));
        }
        List<Criteria> postLookupCriteria = buildPostLookupCriteria(filter);
        if (!postLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(postLookupCriteria.toArray(new Criteria[0]))));
        }
        Aggregation aggregation = newAggregation(operations);
        return mongoTemplate.aggregate(aggregation, "receipt", Receipt.class).getMappedResults();
    }
    private long countReceiptsWithFilters(ReceiptRequestTable filter) {
        List<Criteria> preLookupCriteria = buildPreLookupCriteria(filter);
        if (preLookupCriteria.isEmpty() && !needsBillLookup(filter)) {
            return mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), Receipt.class);
        }
        if (!needsBillLookup(filter)) {
            org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
            query.addCriteria(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0])));
            return mongoTemplate.count(query, Receipt.class);
        }
        return countWithAggregation(filter, preLookupCriteria);
    }
    private long countWithAggregation(ReceiptRequestTable filter, List<Criteria> preLookupCriteria) {
        List<AggregationOperation> operations = new ArrayList<>();
        if (!preLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(preLookupCriteria.toArray(new Criteria[0]))));
        }
        if (needsBillLookup(filter)) {
            operations.add(lookup("bill", "bill", "_id", "billData"));
            operations.add(unwind("billData", true));
        }
        List<Criteria> postLookupCriteria = buildPostLookupCriteria(filter);
        if (!postLookupCriteria.isEmpty()) {
            operations.add(match(new Criteria().andOperator(postLookupCriteria.toArray(new Criteria[0]))));
        }
        operations.add(count().as("total"));
        Aggregation aggregation = newAggregation(operations);
        org.bson.Document result = mongoTemplate.aggregate(aggregation, "receipt", org.bson.Document.class)
                .getUniqueMappedResult();
        return result != null ? ((Number) result.get("total")).longValue() : 0;
    }
    private List<Criteria> buildPreLookupCriteria(ReceiptRequestTable filter) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(filter.getReceiptNumber())) {
            String receiptPattern = filter.getReceiptNumber().trim();
            criteriaList.add(Criteria.where("_id").regex(receiptPattern, "i"));
        }
        if (StringUtils.hasText(filter.getBillNumber())) {
        }
        if (StringUtils.hasText(filter.getDate())) {
            String dateText = filter.getDate().trim();
            LocalDate parsedDate = parseDate(dateText);
            if (parsedDate != null) {
                criteriaList.add(Criteria.where("localDate").is(parsedDate));
            }
        }
        return criteriaList;
    }
    private List<Criteria> buildPostLookupCriteria(ReceiptRequestTable filter) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(filter.getBillNumber())) {
            String billPattern = filter.getBillNumber().trim();
            criteriaList.add(Criteria.where("billData._id").regex(billPattern, "i"));
        }
        return criteriaList;
    }
    private boolean needsBillLookup(ReceiptRequestTable filter) {
        return StringUtils.hasText(filter.getBillNumber());
    }
    private LocalDate parseDate(String dateText) {
        try {
            return LocalDate.parse(dateText, DATE_FORMATTER);
        } catch (Exception ex) {
            log.debug("Failed to parse date '{}' with formatter '{}': {}", dateText, DATE_FORMATTER, ex.getMessage());
            return null;
        }
    }
}
