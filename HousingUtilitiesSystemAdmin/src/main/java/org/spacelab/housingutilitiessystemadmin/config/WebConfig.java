package org.spacelab.housingutilitiessystemadmin.config;

import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
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
    private String projectPath;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToObjectIdConverter);
        registry.addConverter(stringToDoubleConverter);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:static/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");

    }
}
