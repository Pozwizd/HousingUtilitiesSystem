package org.spacelab.housingutilitiessystemchairman.config.database;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Set;
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DatabaseCleanupService {
    private final MongoTemplate mongoTemplate;
    @PreDestroy
    public void cleanupDatabase() {
        try {
            log.info("Начинаем очистку базы данных...");
            Set<String> collectionNames = mongoTemplate.getCollectionNames();
            for (String collectionName : collectionNames) {
                try {
                    mongoTemplate.getCollection(collectionName).drop();
                    log.info("Очищена коллекция: {}", collectionName);
                } catch (Exception e) {
                    log.warn("Не удалось очистить коллекцию {}: {}", collectionName, e.getMessage());
                }
            }
            log.info("База данных очищена при завершении приложения");
        } catch (Exception e) {
            log.error("Ошибка при очистке базы данных: {}", e.getMessage(), e);
        }
    }
}
