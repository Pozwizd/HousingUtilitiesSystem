package org.spacelab.housingutilitiessystemchairman;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
@SpringBootApplication(exclude = {
        RedisRepositoriesAutoConfiguration.class,
        ElasticsearchRepositoriesAutoConfiguration.class,
        ReactiveElasticsearchRepositoriesAutoConfiguration.class
})
public class HousingUtilitiesSystemChairmanApplication {
    public static void main(String[] args) {
        SpringApplication.run(HousingUtilitiesSystemChairmanApplication.class, args);
    }
}
