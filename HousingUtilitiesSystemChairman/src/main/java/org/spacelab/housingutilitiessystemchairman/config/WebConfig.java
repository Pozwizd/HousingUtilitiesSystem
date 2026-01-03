package org.spacelab.housingutilitiessystemchairman.config;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.spacelab.housingutilitiessystemchairman.config.converter.StringToDoubleConverter;
import org.spacelab.housingutilitiessystemchairman.config.converter.StringToObjectIdConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.http.HttpClient;
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final StringToObjectIdConverter stringToObjectIdConverter;
    private final StringToDoubleConverter stringToDoubleConverter;
    @Bean
    public Faker faker() {
        return new Faker();
    }
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
    @Value("${file.upload.dir}")
    private String uploadDir;
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToObjectIdConverter);
        registry.addConverter(stringToDoubleConverter);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:static/");
        String resourceLocation = uploadDir.endsWith("/") ? "file:" + uploadDir : "file:" + uploadDir + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
