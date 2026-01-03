package org.spacelab.housingutilitiessystemadmin.config;

import com.sendgrid.SendGrid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@Slf4j
public class MailConfig {

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean mailSmtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean mailSmtpStarttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.starttls.required}")
    private boolean mailSmtpStarttlsRequired;

    @Value("${spring.mail.properties.mail.smtp.connection-timeout:5000}")
    private int mailSmtpConnectionTimeout;

    @Value("${spring.mail.properties.mail.smtp.timeout:5000}")
    private int mailSmtpTimeout;

    @Value("${spring.mail.properties.mail.smtp.write-timeout:5000}")
    private int mailSmtpWriteTimeout;

    @Value("${spring.sendgrid.api-key}")
    private String sendgridApi;

    @Bean
    @Profile("dev")
    public SendGrid devSendGrid(@Value("${spring.sendgrid.api-key}") String apiKey) {
        log.info("Creating SendGrid bean for DEV profile");
        // dev с режимом тестирования
        return new SendGrid(apiKey, true);  // test mode = true
    }

    @Bean
    @Profile("prod")
    public SendGrid prodSendGrid(@Value("${spring.sendgrid.api-key}") String apiKey) {
        log.info("Creating SendGrid bean for PROD profile");
        // prod без режима тестирования
        return new SendGrid(apiKey, false);  // test mode = false
    }

    @Bean
    @Profile("dev")
    public JavaMailSender devJavaMailSender() {
        return createJavaMailSender(true);
    }

    @Bean
    @Profile("prod")
    public JavaMailSender prodJavaMailSender() {
        return createJavaMailSender(false);
    }

    private JavaMailSender createJavaMailSender(boolean debugMode) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(mailSmtpAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(mailSmtpStarttlsEnable));
        props.put("mail.smtp.starttls.required", String.valueOf(mailSmtpStarttlsRequired));
        props.put("mail.smtp.connection-timeout", String.valueOf(mailSmtpConnectionTimeout));
        props.put("mail.smtp.timeout", String.valueOf(mailSmtpTimeout));
        props.put("mail.smtp.write-timeout", String.valueOf(mailSmtpWriteTimeout));
        props.put("mail.debug", String.valueOf(debugMode));

        return mailSender;
    }




}
