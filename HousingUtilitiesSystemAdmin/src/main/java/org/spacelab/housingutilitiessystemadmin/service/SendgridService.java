package org.spacelab.housingutilitiessystemadmin.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendgridService {

    private final SendGrid sendGrid;
    private final SpringTemplateEngine templateEngine;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    /**
     * Отправка простого HTML письма
     *
     * @param to      адрес получателя
     * @param subject тема письма
     * @param body    HTML содержимое письма
     */
    public void sendEmail(String to, String subject, String body) throws IOException {
        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, toEmail, content);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        log.info("Email sent to: {}, response status: {}", to, response.getStatusCode());
    }

    /**
     * Отправка письма по умолчанию с использованием шаблона email/emailTemplate.html
     *
     * @param to      адрес получателя
     * @param subject тема письма
     * @param link    ссылка для действия (подставляется в шаблон как переменная "link")
     */
    public void sendDefaultTemplateEmail(String to, String subject, String link) {
        try {
            Context context = new Context();
            context.setVariable("link", link);
            String htmlBody = templateEngine.process("email/emailTemplate", context);
            sendEmail(to, subject, htmlBody);
            log.debug("Default template email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send default template email to: {}", to, e);
            throw new RuntimeException("Failed to send default template email", e);
        }
    }

    /**
     * Отправка письма с кастомным шаблоном и переменными
     *
     * @param to           адрес получателя
     * @param subject      тема письма
     * @param templateName имя шаблона (например: "email/confirmationEmail")
     * @param variables    карта переменных для подстановки в шаблон
     */
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);
            String htmlBody = templateEngine.process(templateName, context);
            sendEmail(to, subject, htmlBody);
            log.debug("Template email sent successfully to: {} using template: {}", to, templateName);
        } catch (Exception e) {
            log.error("Failed to send template email to: {} with template: {}", to, templateName, e);
            throw new RuntimeException("Failed to send template email", e);
        }
    }

    /**
     * Отправка письма верификации с использованием шаблона
     *
     * @param to               адрес получателя
     * @param userName         имя пользователя
     * @param verificationLink ссылка подтверждения
     */
    public void sendVerificationEmail(String to, String userName, String verificationLink) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("verificationLink", verificationLink);
            String htmlBody = templateEngine.process("email/verificationEmail", context);
            sendEmail(to, "Подтверждение вашей почты", htmlBody);
            log.debug("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Отправка письма сброса пароля
     *
     * @param to              адрес получателя
     * @param userName        имя пользователя
     * @param resetLink       ссылка для сброса пароля
     * @param expirationHours через сколько часов ссылка истечет
     */
    public void sendPasswordResetEmail(String to, String userName, String resetLink, int expirationHours) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("resetLink", resetLink);
            context.setVariable("expirationHours", expirationHours);
            String htmlBody = templateEngine.process("email/passwordResetEmail", context);
            sendEmail(to, "Сброс пароля", htmlBody);
            log.debug("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
