package org.spacelab.housingutilitiessystemuser.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Getter
@Configuration
public class AiConfig {

    @Value("${ai.gpt4free.url:http://localhost:8090}")
    private String gpt4freeUrl;

    @Value("${ai.gpt4free.provider:PollinationsAI}")
    private String provider;

    @Value("${ai.gpt4free.model:}")
    private String model;

    @Bean
    public RestTemplate aiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        return new RestTemplate(factory);
    }

}
