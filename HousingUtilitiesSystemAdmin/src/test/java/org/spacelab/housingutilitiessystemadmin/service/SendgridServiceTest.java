package org.spacelab.housingutilitiessystemadmin.service;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendgridService Tests")
class SendgridServiceTest {

    @Mock
    private SendGrid sendGrid;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private SendgridService sendgridService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sendgridService, "fromEmail", "test@example.com");
    }

    @Nested
    @DisplayName("sendEmail Tests")
    class SendEmailTests {
        @Test
        @DisplayName("Should send email successfully")
        void sendEmail_shouldSendSuccessfully() throws IOException {
            Response response = new Response();
            response.setStatusCode(202);
            when(sendGrid.api(any(Request.class))).thenReturn(response);

            sendgridService.sendEmail("to@test.com", "Subject", "<p>Body</p>");

            verify(sendGrid).api(any(Request.class));
        }

        @Test
        @DisplayName("Should throw exception when API fails")
        void sendEmail_shouldThrowException_whenApiFails() throws IOException {
            when(sendGrid.api(any(Request.class))).thenThrow(new IOException("API Error"));

            assertThatThrownBy(() -> sendgridService.sendEmail("to@test.com", "Subject", "Body"))
                    .isInstanceOf(IOException.class)
                    .hasMessage("API Error");
        }
    }

    @Nested
    @DisplayName("sendDefaultTemplateEmail Tests")
    class SendDefaultTemplateEmailTests {
        @Test
        @DisplayName("Should send default template email successfully")
        void sendDefaultTemplateEmail_shouldSendSuccessfully() throws IOException {
            when(templateEngine.process(eq("email/emailTemplate"), any(Context.class)))
                    .thenReturn("<html>Template content</html>");
            Response response = new Response();
            response.setStatusCode(202);
            when(sendGrid.api(any(Request.class))).thenReturn(response);

            sendgridService.sendDefaultTemplateEmail("to@test.com", "Subject", "http://link.com");

            verify(templateEngine).process(eq("email/emailTemplate"), any(Context.class));
            verify(sendGrid).api(any(Request.class));
        }

        @Test
        @DisplayName("Should throw exception when template processing fails")
        void sendDefaultTemplateEmail_shouldThrowException_whenTemplateProcessingFails() {
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenThrow(new RuntimeException("Template error"));

            assertThatThrownBy(() -> sendgridService.sendDefaultTemplateEmail("to@test.com", "Subject", "link"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send default template email");
        }
    }

    @Nested
    @DisplayName("sendTemplateEmail Tests")
    class SendTemplateEmailTests {
        @Test
        @DisplayName("Should send template email with custom variables")
        void sendTemplateEmail_shouldSendWithCustomVariables() throws IOException {
            when(templateEngine.process(eq("email/customTemplate"), any(Context.class)))
                    .thenReturn("<html>Custom template</html>");
            Response response = new Response();
            response.setStatusCode(202);
            when(sendGrid.api(any(Request.class))).thenReturn(response);

            Map<String, Object> variables = new HashMap<>();
            variables.put("key1", "value1");
            variables.put("key2", "value2");

            sendgridService.sendTemplateEmail("to@test.com", "Subject", "email/customTemplate", variables);

            verify(templateEngine).process(eq("email/customTemplate"), any(Context.class));
            verify(sendGrid).api(any(Request.class));
        }

        @Test
        @DisplayName("Should throw exception when sending fails")
        void sendTemplateEmail_shouldThrowException_whenSendingFails() {
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenThrow(new RuntimeException("Template error"));

            assertThatThrownBy(() -> sendgridService.sendTemplateEmail("to@test.com", "Subject", "template", Map.of()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send template email");
        }
    }

    @Nested
    @DisplayName("sendVerificationEmail Tests")
    class SendVerificationEmailTests {
        @Test
        @DisplayName("Should send verification email successfully")
        void sendVerificationEmail_shouldSendSuccessfully() throws IOException {
            when(templateEngine.process(eq("email/verificationEmail"), any(Context.class)))
                    .thenReturn("<html>Verification email</html>");
            Response response = new Response();
            response.setStatusCode(202);
            when(sendGrid.api(any(Request.class))).thenReturn(response);

            sendgridService.sendVerificationEmail("to@test.com", "UserName", "http://verify.link");

            verify(templateEngine).process(eq("email/verificationEmail"), any(Context.class));
            verify(sendGrid).api(any(Request.class));
        }

        @Test
        @DisplayName("Should throw exception when verification email fails")
        void sendVerificationEmail_shouldThrowException_whenFails() {
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenThrow(new RuntimeException("Error"));

            assertThatThrownBy(() -> sendgridService.sendVerificationEmail("to@test.com", "User", "link"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send verification email");
        }
    }

    @Nested
    @DisplayName("sendPasswordResetEmail Tests")
    class SendPasswordResetEmailTests {
        @Test
        @DisplayName("Should send password reset email successfully")
        void sendPasswordResetEmail_shouldSendSuccessfully() throws IOException {
            when(templateEngine.process(eq("email/passwordResetEmail"), any(Context.class)))
                    .thenReturn("<html>Reset email</html>");
            Response response = new Response();
            response.setStatusCode(202);
            when(sendGrid.api(any(Request.class))).thenReturn(response);

            sendgridService.sendPasswordResetEmail("to@test.com", "UserName", "http://reset.link", 24);

            verify(templateEngine).process(eq("email/passwordResetEmail"), any(Context.class));
            verify(sendGrid).api(any(Request.class));
        }

        @Test
        @DisplayName("Should throw exception when password reset email fails")
        void sendPasswordResetEmail_shouldThrowException_whenFails() {
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenThrow(new RuntimeException("Error"));

            assertThatThrownBy(() -> sendgridService.sendPasswordResetEmail("to@test.com", "User", "link", 24))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send password reset email");
        }
    }
}
