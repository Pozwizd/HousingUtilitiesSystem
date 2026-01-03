package org.spacelab.housingutilitiessystemuser.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
@Slf4j
public class JavaMailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.debug("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.debug("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody, String from) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.debug("HTML email sent successfully to: {} from: {}", to, from);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {} from: {}", to, from, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    public void sendToken(String token, String to, HttpServletRequest httpRequest) {
        log.info("sendToken() - Sending password reset token to {}", to);
        try {
            String subject = "Встановлення нового паролю";
            String htmlBody = buildPasswordResetLink(token, httpRequest);
            sendHtmlEmail(to, subject, htmlBody);
            log.info("sendToken() - Password reset token was sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset token to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildPasswordResetLink(String token, HttpServletRequest httpRequest) {
        Context context = new Context();

        final String fullUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                .build()
                .toUriString();

        log.info("buildPasswordResetLink() - Full URL: {}", fullUrl);

        int lastSlashIndex = fullUrl.lastIndexOf("/");
        String baseUrl = fullUrl.substring(0, lastSlashIndex);
        String resetLink = baseUrl + "/changePassword?token=" + token;

        log.info("buildPasswordResetLink() - Reset link: {}", resetLink);

        context.setVariable("link", resetLink);
        return templateEngine.process("email/emailTemplate", context);
    }
}
