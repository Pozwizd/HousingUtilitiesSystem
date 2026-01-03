package org.spacelab.housingutilitiessystemadmin.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

/**
 * Слушатель событий MongoDB для автоматической индексации пользователей в Elasticsearch
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserIndexingListener extends AbstractMongoEventListener<User> {
    
    private final UserSearchService userSearchService;
    
    /**
     * Автоматическая индексация после сохранения пользователя (create/update)
     */
    @Override
    public void onAfterSave(AfterSaveEvent<User> event) {
        User user = event.getSource();
        try {
            userSearchService.indexUser(user);
            log.debug("Auto-indexed user: {} {}", user.getFirstName(), user.getLastName());
        } catch (Exception e) {
            log.error("Failed to auto-index user with id: {}", user.getId(), e);
        }
    }
    
    /**
     * Автоматическое удаление из индекса после удаления пользователя
     */
    @Override
    public void onAfterDelete(AfterDeleteEvent<User> event) {
        Object id = event.getSource().get("_id");
        if (id != null) {
            try {
                userSearchService.deleteFromIndex(id.toString());
                log.debug("Auto-deleted user from index: {}", id);
            } catch (Exception e) {
                log.error("Failed to delete user from index: {}", id, e);
            }
        }
    }
}
