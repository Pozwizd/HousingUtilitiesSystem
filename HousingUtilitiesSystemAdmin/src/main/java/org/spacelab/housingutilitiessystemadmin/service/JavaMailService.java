package org.spacelab.housingutilitiessystemadmin.service;

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

    /**
     * Отправка простого текстового письма
     *
     * @param to      адрес получателя
     * @param subject тема письма
     * @param text    текст письма
     */
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

    /**
     * Отправка письма с указанием отправителя
     *
     * @param to      адрес получателя
     * @param subject тема письма
     * @param text    текст письма
     * @param from    адрес отправителя
     */
    public void sendSimpleEmail(String to, String subject, String text, String from) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.debug("Simple email sent successfully to: {} from: {}", to, from);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {} from: {}", to, from, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Отправка HTML письма
     *
     * @param to       адрес получателя
     * @param subject  тема письма
     * @param htmlBody HTML содержимое письма
     */
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

    /**
     * Отправка HTML письма с указанием отправителя
     *
     * @param to       адрес получателя
     * @param subject  тема письма
     * @param htmlBody HTML содержимое письма
     * @param from     адрес отправителя
     */
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

    /**
     * Отправка письма нескольким получателям
     *
     * @param to      массив адресов получателей
     * @param subject тема письма
     * @param text    текст письма
     */
    public void sendSimpleEmailToMultiple(String[] to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.debug("Simple email sent successfully to multiple recipients: {}", (Object) to);
        } catch (Exception e) {
            log.error("Failed to send simple email to multiple recipients", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Отправка письма с копией (CC) и скрытой копией (BCC)
     *
     * @param to      адрес получателя
     * @param cc      адреса для копии
     * @param bcc     адреса для скрытой копии
     * @param subject тема письма
     * @param text    текст письма
     */
    public void sendEmailWithCcAndBcc(String to, String[] cc, String[] bcc, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            if (cc != null && cc.length > 0) {
                message.setCc(cc);
            }
            if (bcc != null && bcc.length > 0) {
                message.setBcc(bcc);
            }
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.debug("Email with CC/BCC sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email with CC/BCC to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Отправка письма по умолчанию с использованием шаблона email/emailTemplate.html
     *
     * @param to       адрес получателя
     * @param subject  тема письма
     * @param link     ссылка для действия (подставляется в шаблон как переменная "link")
     */
    public void sendDefaultTemplateEmail(String to, String subject, String link) {
        try {
            Context context = new Context();
            context.setVariable("link", link);
            String htmlBody = templateEngine.process("email/emailTemplate", context);
            sendHtmlEmail(to, subject, htmlBody);
            log.debug("Default template email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send default template email to: {}", to, e);
            throw new RuntimeException("Failed to send default template email", e);
        }
    }

    /**
     * Отправка письма с токеном для сброса пароля
     *
     * @param token       токен для сброса пароля
     * @param to          адрес получателя
     * @param httpRequest текущий HTTP запрос для построения URL
     */
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

    /**
     * Отправка письма с токеном для сброса пароля с указанием отправителя
     *
     * @param token       токен для сброса пароля
     * @param to          адрес получателя
     * @param from        адрес отправителя
     * @param httpRequest текущий HTTP запрос для построения URL
     */
    public void sendToken(String token, String to, String from, HttpServletRequest httpRequest) {
        log.info("sendToken() - Sending password reset token to {} from {}", to, from);
        try {
            String subject = "Встановлення нового паролю";
            String htmlBody = buildPasswordResetLink(token, httpRequest);
            sendHtmlEmail(to, subject, htmlBody, from);
            log.info("sendToken() - Password reset token was sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset token to: {} from: {}", to, from, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Построение HTML контента письма с ссылкой для изменения пароля
     *
     * @param token       токен для сброса пароля
     * @param httpRequest текущий HTTP запрос для получения базового URL
     * @return HTML контент с ссылкой для изменения пароля
     */
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
