package org.spacelab.housingutilitiessystemadmin.repository;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemadmin.security.RememberMeTokenDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class MongoTokenRepository implements PersistentTokenRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        RememberMeTokenDocument doc = new RememberMeTokenDocument();
        doc.setUsername(token.getUsername());
        doc.setSeries(token.getSeries());
        doc.setTokenValue(token.getTokenValue());
        doc.setDate(token.getDate());

        mongoTemplate.save(doc);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        Query query = new Query(Criteria.where("series").is(series));
        Update update = new Update()
                .set("tokenValue", tokenValue)
                .set("date", lastUsed);

        mongoTemplate.updateFirst(query, update, RememberMeTokenDocument.class);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        Query query = new Query(Criteria.where("series").is(seriesId));
        RememberMeTokenDocument doc = mongoTemplate.findOne(query, RememberMeTokenDocument.class);

        if (doc == null) {
            return null;
        }

        return new PersistentRememberMeToken(
                doc.getUsername(),
                doc.getSeries(),
                doc.getTokenValue(),
                doc.getDate()
        );
    }

    @Override
    public void removeUserTokens(String username) {
        Query query = new Query(Criteria.where("username").is(username));
        mongoTemplate.remove(query, RememberMeTokenDocument.class);
    }
}
