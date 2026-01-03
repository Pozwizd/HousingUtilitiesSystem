package org.spacelab.housingutilitiessystemadmin.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.spacelab.housingutilitiessystemadmin.search")
public class ElasticsearchConfig {

}
