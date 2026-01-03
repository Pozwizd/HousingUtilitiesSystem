package org.spacelab.housingutilitiessystemadmin.service;

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
    @DisplayName("sendSimpleEmail Tests")
    class SendSimpleEmailTests {
        @Test
        @DisplayName("Should send simple email successfully")
        void sendSimpleEmail_shouldSendSuccessfully() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            javaMailService.sendSimpleEmail("to@test.com", "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should throw exception when sending fails")
        void sendSimpleEmail_shouldThrowException_whenFails() {
            doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

            assertThatThrownBy(() -> javaMailService.sendSimpleEmail("to@test.com", "Subject", "Text"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send email");
        }

        @Test
        @DisplayName("Should send simple email with from address")
        void sendSimpleEmail_withFrom_shouldSendSuccessfully() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            javaMailService.sendSimpleEmail("to@test.com", "Subject", "Text", "from@test.com");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should throw exception when sending with from fails")
        void sendSimpleEmail_withFrom_shouldThrowException_whenFails() {
            doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

            assertThatThrownBy(() -> javaMailService.sendSimpleEmail("to@test.com", "Subject", "Text", "from@test.com"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send email");
        }
    }

    @Nested
    @DisplayName("sendHtmlEmail Tests")
    class SendHtmlEmailTests {
        @Test
        @DisplayName("Should send HTML email successfully")
        void sendHtmlEmail_shouldSendSuccessfully() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            javaMailService.sendHtmlEmail("to@test.com", "Subject", "<p>HTML</p>");

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("Should send HTML email with from address")
        void sendHtmlEmail_withFrom_shouldSendSuccessfully() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            javaMailService.sendHtmlEmail("to@test.com", "Subject", "<p>HTML</p>", "from@test.com");

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("sendSimpleEmailToMultiple Tests")
    class SendSimpleEmailToMultipleTests {
        @Test
        @DisplayName("Should send email to multiple recipients")
        void sendSimpleEmailToMultiple_shouldSendSuccessfully() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));
            String[] recipients = {"to1@test.com", "to2@test.com"};

            javaMailService.sendSimpleEmailToMultiple(recipients, "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should throw exception when multiple recipients fails")
        void sendSimpleEmailToMultiple_shouldThrowException_whenFails() {
            doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));
            String[] recipients = {"to1@test.com", "to2@test.com"};

            assertThatThrownBy(() -> javaMailService.sendSimpleEmailToMultiple(recipients, "Subject", "Text"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send email");
        }
    }

    @Nested
    @DisplayName("sendEmailWithCcAndBcc Tests")
    class SendEmailWithCcAndBccTests {
        @Test
        @DisplayName("Should send email with CC and BCC")
        void sendEmailWithCcAndBcc_shouldSendSuccessfully() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));
            String[] cc = {"cc@test.com"};
            String[] bcc = {"bcc@test.com"};

            javaMailService.sendEmailWithCcAndBcc("to@test.com", cc, bcc, "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should send email with null CC")
        void sendEmailWithCcAndBcc_withNullCc_shouldSendSuccessfully() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));
            String[] bcc = {"bcc@test.com"};

            javaMailService.sendEmailWithCcAndBcc("to@test.com", null, bcc, "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should send email with null BCC")
        void sendEmailWithCcAndBcc_withNullBcc_shouldSendSuccessfully() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));
            String[] cc = {"cc@test.com"};

            javaMailService.sendEmailWithCcAndBcc("to@test.com", cc, null, "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should send email with empty CC and BCC")
        void sendEmailWithCcAndBcc_withEmptyArrays_shouldSendSuccessfully() {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));
            String[] cc = {};
            String[] bcc = {};

            javaMailService.sendEmailWithCcAndBcc("to@test.com", cc, bcc, "Subject", "Text");

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should throw exception when CC/BCC email fails")
        void sendEmailWithCcAndBcc_shouldThrowException_whenFails() {
            doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));
            String[] cc = {"cc@test.com"};
            String[] bcc = {"bcc@test.com"};

            assertThatThrownBy(() -> javaMailService.sendEmailWithCcAndBcc("to@test.com", cc, bcc, "Subject", "Text"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send email");
        }
    }

    @Nested
    @DisplayName("sendDefaultTemplateEmail Tests")
    class SendDefaultTemplateEmailTests {
        @Test
        @DisplayName("Should send default template email successfully")
        void sendDefaultTemplateEmail_shouldSendSuccessfully() {
            when(templateEngine.process(eq("email/emailTemplate"), any(Context.class)))
                    .thenReturn("<html>Template</html>");
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            javaMailService.sendDefaultTemplateEmail("to@test.com", "Subject", "http://link.com");

            verify(templateEngine).process(eq("email/emailTemplate"), any(Context.class));
            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("Should throw exception when template email fails")
        void sendDefaultTemplateEmail_shouldThrowException_whenFails() {
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenThrow(new RuntimeException("Template error"));

            assertThatThrownBy(() -> javaMailService.sendDefaultTemplateEmail("to@test.com", "Subject", "link"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send default template email");
        }
    }

    @Nested
    @DisplayName("sendToken Tests")
    class SendTokenTests {
        @BeforeEach
        void setUpRequest() {
            when(httpServletRequest.getScheme()).thenReturn("http");
            when(httpServletRequest.getServerName()).thenReturn("localhost");
            when(httpServletRequest.getServerPort()).thenReturn(8080);
            when(httpServletRequest.getRequestURI()).thenReturn("/api/resetPassword");
        }

        @Test
        @DisplayName("Should send token email successfully")
        void sendToken_shouldSendSuccessfully() {
            when(templateEngine.process(eq("email/emailTemplate"), any(Context.class)))
                    .thenReturn("<html>Reset Link</html>");
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            javaMailService.sendToken("token123", "to@test.com", httpServletRequest);

            verify(templateEngine).process(eq("email/emailTemplate"), any(Context.class));
            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("Should send token email with from address")
        void sendToken_withFrom_shouldSendSuccessfully() {
            when(templateEngine.process(eq("email/emailTemplate"), any(Context.class)))
                    .thenReturn("<html>Reset Link</html>");
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            javaMailService.sendToken("token123", "to@test.com", "from@test.com", httpServletRequest);

            verify(templateEngine).process(eq("email/emailTemplate"), any(Context.class));
            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("Should throw exception when token email fails")
        void sendToken_shouldThrowException_whenFails() {
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenThrow(new RuntimeException("Template error"));

            assertThatThrownBy(() -> javaMailService.sendToken("token", "to@test.com", httpServletRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send password reset email");
        }

        @Test
        @DisplayName("Should throw exception when token email with from fails")
        void sendToken_withFrom_shouldThrowException_whenFails() {
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenThrow(new RuntimeException("Template error"));

            assertThatThrownBy(() -> javaMailService.sendToken("token", "to@test.com", "from@test.com", httpServletRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send password reset email");
        }
    }
}
