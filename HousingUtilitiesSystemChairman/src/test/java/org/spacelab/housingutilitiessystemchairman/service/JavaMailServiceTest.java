package org.spacelab.housingutilitiessystemchairman.service;

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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private HttpServletRequest httpRequest;

    @InjectMocks
    private JavaMailService javaMailService;

    @BeforeEach
    void setUp() {
        // Common setup
    }

    @Nested
    @DisplayName("Simple Email")
    class SimpleEmail {
        @Test
        @DisplayName("Should send simple email")
        void sendSimpleEmail_shouldSend() {
            javaMailService.sendSimpleEmail("to@test.com", "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should send simple email with from")
        void sendSimpleEmail_withFrom_shouldSend() {
            javaMailService.sendSimpleEmail("to@test.com", "Subject", "Text", "from@test.com");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should throw exception on send failure")
        void sendSimpleEmail_shouldThrowException() {
            doThrow(new RuntimeException("Send failed")).when(mailSender).send(any(SimpleMailMessage.class));

            assertThatThrownBy(() -> javaMailService.sendSimpleEmail("to@test.com", "Subject", "Text"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("HTML Email")
    class HtmlEmail {
        @Test
        @DisplayName("Should send HTML email")
        void sendHtmlEmail_shouldSend() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            javaMailService.sendHtmlEmail("to@test.com", "Subject", "<html>Body</html>");

            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send HTML email with from")
        void sendHtmlEmail_withFrom_shouldSend() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            javaMailService.sendHtmlEmail("to@test.com", "Subject", "<html>Body</html>", "from@test.com");

            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Multiple Recipients")
    class MultipleRecipients {
        @Test
        @DisplayName("Should send to multiple recipients")
        void sendSimpleEmailToMultiple_shouldSend() {
            String[] recipients = {"a@test.com", "b@test.com"};

            javaMailService.sendSimpleEmailToMultiple(recipients, "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should throw exception on send failure")
        void sendSimpleEmailToMultiple_shouldThrowException() {
            doThrow(new RuntimeException("Send failed")).when(mailSender).send(any(SimpleMailMessage.class));
            String[] recipients = {"a@test.com"};

            assertThatThrownBy(() -> javaMailService.sendSimpleEmailToMultiple(recipients, "Subject", "Text"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("CC and BCC")
    class CcAndBcc {
        @Test
        @DisplayName("Should send with CC and BCC")
        void sendEmailWithCcAndBcc_shouldSend() {
            String[] cc = {"cc@test.com"};
            String[] bcc = {"bcc@test.com"};

            javaMailService.sendEmailWithCcAndBcc("to@test.com", cc, bcc, "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should send with null CC and BCC")
        void sendEmailWithCcAndBcc_shouldSend_withNullCcBcc() {
            javaMailService.sendEmailWithCcAndBcc("to@test.com", null, null, "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should send with empty CC and BCC")
        void sendEmailWithCcAndBcc_shouldSend_withEmptyCcBcc() {
            javaMailService.sendEmailWithCcAndBcc("to@test.com", new String[]{}, new String[]{}, "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }
    }

    @Nested
    @DisplayName("Template Email")
    class TemplateEmail {
        @Test
        @DisplayName("Should send default template email")
        void sendDefaultTemplateEmail_shouldSend() {
            when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Link</html>");
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            javaMailService.sendDefaultTemplateEmail("to@test.com", "Subject", "http://link.com");

            verify(templateEngine).process(eq("email/emailTemplate"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should throw exception on template processing failure")
        void sendDefaultTemplateEmail_shouldThrowException() {
            when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

            assertThatThrownBy(() -> javaMailService.sendDefaultTemplateEmail("to@test.com", "Subject", "http://link.com"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Password Reset Token")
    class PasswordResetToken {
        @Test
        @DisplayName("Should send token")
        void sendToken_shouldSend() {
            when(httpRequest.getRequestURI()).thenReturn("/forgot-password");
            when(httpRequest.getScheme()).thenReturn("http");
            when(httpRequest.getServerName()).thenReturn("localhost");
            when(httpRequest.getServerPort()).thenReturn(8080);
            when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Reset Link</html>");
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            javaMailService.sendToken("test-token", "to@test.com", httpRequest);

            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send token with from")
        void sendToken_withFrom_shouldSend() {
            when(httpRequest.getRequestURI()).thenReturn("/forgot-password");
            when(httpRequest.getScheme()).thenReturn("http");
            when(httpRequest.getServerName()).thenReturn("localhost");
            when(httpRequest.getServerPort()).thenReturn(8080);
            when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Reset Link</html>");
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            javaMailService.sendToken("test-token", "to@test.com", "from@test.com", httpRequest);

            verify(mailSender).send(mimeMessage);
        }
    }
}
