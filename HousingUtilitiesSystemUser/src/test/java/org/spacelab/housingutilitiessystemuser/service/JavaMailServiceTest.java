package org.spacelab.housingutilitiessystemuser.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JavaMailService Tests")
class JavaMailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private JavaMailService javaMailService;

    @Nested
    @DisplayName("Send Simple Email")
    class SendSimpleEmail {
        @Test
        @DisplayName("Should send simple email successfully")
        void sendSimpleEmail_shouldSend() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            javaMailService.sendSimpleEmail("to@test.com", "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should throw exception on failure")
        void sendSimpleEmail_shouldThrowException() {
            doThrow(new MailSendException("Send failed")).when(mailSender).send(any(SimpleMailMessage.class));

            assertThatThrownBy(() -> javaMailService.sendSimpleEmail("to@test.com", "Subject", "Text"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Send HTML Email")
    class SendHtmlEmail {
        @Test
        @DisplayName("Should send HTML email successfully")
        void sendHtmlEmail_shouldSend() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            javaMailService.sendHtmlEmail("to@test.com", "Subject", "<html>Body</html>");

            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should throw exception on HTML email failure")
        void sendHtmlEmail_shouldThrowException() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailSendException("Send failed")).when(mailSender).send(any(MimeMessage.class));

            // MailSendException is not caught, it propagates directly
            assertThatThrownBy(() -> javaMailService.sendHtmlEmail("to@test.com", "Subject", "<html>Body</html>"))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should send HTML email with from address")
        void sendHtmlEmail_withFrom_shouldSend() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            javaMailService.sendHtmlEmail("to@test.com", "Subject", "<html>Body</html>", "from@test.com");

            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should throw exception on HTML email with from failure")
        void sendHtmlEmail_withFrom_shouldThrowException() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailSendException("Send failed")).when(mailSender).send(any(MimeMessage.class));

            // MailSendException is not caught, it propagates directly
            assertThatThrownBy(
                    () -> javaMailService.sendHtmlEmail("to@test.com", "Subject", "<html>Body</html>", "from@test.com"))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should throw RuntimeException when MessagingException occurs in sendHtmlEmail")
        void sendHtmlEmail_shouldThrowRuntimeExceptionOnMessagingException() throws Exception {
            MimeMessage realMimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
            doThrow(new MessagingException("Messaging error")).when(realMimeMessage)
                    .setContent(any(jakarta.mail.Multipart.class));

            assertThatThrownBy(() -> javaMailService.sendHtmlEmail("to@test.com", "Subject", "<html>Body</html>"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send HTML email");
        }

        @Test
        @DisplayName("Should throw RuntimeException when MessagingException occurs in sendHtmlEmail with from")
        void sendHtmlEmail_withFrom_shouldThrowRuntimeExceptionOnMessagingException() throws Exception {
            MimeMessage realMimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
            doThrow(new MessagingException("Messaging error")).when(realMimeMessage)
                    .setContent(any(jakarta.mail.Multipart.class));

            assertThatThrownBy(
                    () -> javaMailService.sendHtmlEmail("to@test.com", "Subject", "<html>Body</html>", "from@test.com"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send HTML email");
        }
    }

    @Nested
    @DisplayName("Send Token")
    class SendToken {
        @Test
        @DisplayName("Should send password reset token")
        void sendToken_shouldSend() throws Exception {
            when(httpServletRequest.getRequestURI()).thenReturn("/forgot-password");
            when(httpServletRequest.getScheme()).thenReturn("http");
            when(httpServletRequest.getServerName()).thenReturn("localhost");
            when(httpServletRequest.getServerPort()).thenReturn(8080);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/emailTemplate"), any(Context.class)))
                    .thenReturn("<html>Reset Link</html>");
            doNothing().when(mailSender).send(any(MimeMessage.class));

            javaMailService.sendToken("test-token", "to@test.com", httpServletRequest);

            verify(mailSender).send(mimeMessage);
            verify(templateEngine).process(eq("email/emailTemplate"), any(Context.class));
        }

        @Test
        @DisplayName("Should throw exception when token send fails")
        void sendToken_shouldThrowException() throws Exception {
            when(httpServletRequest.getRequestURI()).thenReturn("/forgot-password");
            when(httpServletRequest.getScheme()).thenReturn("http");
            when(httpServletRequest.getServerName()).thenReturn("localhost");
            when(httpServletRequest.getServerPort()).thenReturn(8080);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/emailTemplate"), any(Context.class)))
                    .thenReturn("<html>Reset Link</html>");
            doThrow(new MailSendException("Send failed")).when(mailSender).send(any(MimeMessage.class));

            assertThatThrownBy(() -> javaMailService.sendToken("test-token", "to@test.com", httpServletRequest))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
