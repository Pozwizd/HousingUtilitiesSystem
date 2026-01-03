package org.spacelab.housingutilitiessystemchairman.config.database;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@RequiredArgsConstructor
public class MongoBulkConfig {
    private final MongoTemplate mongoTemplate;
    public <T> void bulkInsert(List<T> documents, Class<T> entityClass) {
        if (documents.isEmpty()) {
            return;
        }
        documents.forEach(this::ensureObjectHasId);
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, entityClass);
        documents.forEach(bulkOps::insert);
        bulkOps.execute();
    }
    private <T> void ensureObjectHasId(T document) {
        try {
            var idField = document.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object currentId = idField.get(document);
            if (currentId == null || (currentId instanceof String s && s.isEmpty())) {
                idField.set(document, new ObjectId().toString());
            }
        } catch (Exception e) {
            try {
                var fields = document.getClass().getDeclaredFields();
                for (var field : fields) {
                    if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class)) {
                        field.setAccessible(true);
                        Object currentId = field.get(document);
                        if (currentId == null || (currentId instanceof String s && s.isEmpty())) {
                            field.set(document, new ObjectId().toString());
                        }
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
    public <T> void bulkUpsert(List<T> documents, Class<T> entityClass) {
        if (documents.isEmpty()) {
            return;
        }
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, entityClass);
        for (T document : documents) {
            Object documentId = getId(document);
            org.bson.Document mongoDoc = new org.bson.Document();
            mongoTemplate.getConverter().write(document, mongoDoc);
            mongoDoc.remove("_id");
            org.springframework.data.mongodb.core.query.Update update = new org.springframework.data.mongodb.core.query.Update();
            for (String key : mongoDoc.keySet()) {
                update.set(key, mongoDoc.get(key));
            }
            bulkOps.upsert(
                    org.springframework.data.mongodb.core.query.Query.query(
                            org.springframework.data.mongodb.core.query.Criteria.where("_id").is(documentId)
                    ),
                    update
            );
        }
        bulkOps.execute();
    }
    private Object getId(Object document) {
        try {
            var idField = document.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(document);
        } catch (NoSuchFieldException e) {
            try {
                var fields = document.getClass().getDeclaredFields();
                for (var field : fields) {
                    if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class)) {
                        field.setAccessible(true);
                        return field.get(document);
                    }
                }
            } catch (Exception ignored) {
            }
            return new ObjectId();
        } catch (Exception e) {
            return new ObjectId();
        }
    }
}
