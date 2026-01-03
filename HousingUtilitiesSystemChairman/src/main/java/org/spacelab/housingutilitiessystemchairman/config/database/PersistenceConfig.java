package org.spacelab.housingutilitiessystemchairman.config.database;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
@Configuration
@EnableMongoRepositories(basePackages = "org.spacelab.housingutilitiessystemchairman.repository.mongo")
@EnableRedisRepositories(basePackages = "org.spacelab.housingutilitiessystemchairman.repository.redis")
@EnableElasticsearchRepositories(basePackages = "org.spacelab.housingutilitiessystemchairman.repository.elastic")
public class PersistenceConfig {
}
