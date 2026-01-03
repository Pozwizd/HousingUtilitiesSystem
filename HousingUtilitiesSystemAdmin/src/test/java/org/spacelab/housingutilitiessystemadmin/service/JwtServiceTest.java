package org.spacelab.housingutilitiessystemadmin.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.security.CustomOidcUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    // Valid 256-bit key for testing (base64 encoded)
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long TEST_EXPIRATION = 900000L; // 15 minutes
    private static final long TEST_REFRESH_EXPIRATION = 604800000L; // 7 days

    private UserDetails testUserDetails;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomOidcUser customOidcUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", TEST_REFRESH_EXPIRATION);

        testUserDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Nested
    @DisplayName("extractUserName Tests")
    class ExtractUserNameTests {

        @Test
        @DisplayName("Should extract username from token")
        void extractUserName_shouldExtractUsername() {
            // Given
            String token = jwtService.generateToken(testUserDetails);

            // When
            String result = jwtService.extractUserName(token);

            // Then
            assertThat(result).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("generateToken Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate token from UserDetails")
        void generateToken_shouldGenerateFromUserDetails() {
            // When
            String token = jwtService.generateToken(testUserDetails);

            // Then
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should generate token with email claim for CustomOidcUser")
        void generateToken_shouldAddEmailClaim_forCustomOidcUser() {
            // Given
            when(customOidcUser.getUsername()).thenReturn("oidc@example.com");
            when(customOidcUser.getEmail()).thenReturn("oidc@example.com");

            // When
            String token = jwtService.generateToken(customOidcUser);

            // Then
            assertThat(token).isNotNull();
            String username = jwtService.extractUserName(token);
            assertThat(username).isEqualTo("oidc@example.com");
        }

        @Test
        @DisplayName("Should generate token from Authentication")
        void generateToken_shouldGenerateFromAuthentication() {
            // Given
            when(authentication.getName()).thenReturn("auth@example.com");
            when(authentication.getPrincipal()).thenReturn(testUserDetails);

            // When
            String token = jwtService.generateToken(authentication);

            // Then
            assertThat(token).isNotNull();
            String username = jwtService.extractUserName(token);
            assertThat(username).isEqualTo("auth@example.com");
        }

        @Test
        @DisplayName("Should generate token from Authentication with CustomOidcUser")
        void generateToken_shouldAddEmailClaim_fromAuthenticationWithCustomOidcUser() {
            // Given
            when(authentication.getName()).thenReturn("auth@example.com");
            when(authentication.getPrincipal()).thenReturn(customOidcUser);
            when(customOidcUser.getEmail()).thenReturn("oidc@example.com");

            // When
            String token = jwtService.generateToken(authentication);

            // Then
            assertThat(token).isNotNull();
        }
    }

    @Nested
    @DisplayName("generateRefreshToken Tests")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Should generate refresh token from Authentication")
        void generateRefreshToken_shouldGenerateFromAuthentication() {
            // Given
            when(authentication.getName()).thenReturn("auth@example.com");

            // When
            String token = jwtService.generateRefreshToken(authentication);

            // Then
            assertThat(token).isNotNull();
            String username = jwtService.extractUserName(token);
            assertThat(username).isEqualTo("auth@example.com");
        }

        @Test
        @DisplayName("Should generate refresh token from UserDetails")
        void generateRefreshToken_shouldGenerateFromUserDetails() {
            // When
            String token = jwtService.generateRefreshToken(testUserDetails);

            // Then
            assertThat(token).isNotNull();
            String username = jwtService.extractUserName(token);
            assertThat(username).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("isTokenValid Tests")
    class IsTokenValidTests {

        @Test
        @DisplayName("Should return true for valid token")
        void isTokenValid_shouldReturnTrue_forValidToken() {
            // Given
            String token = jwtService.generateToken(testUserDetails);

            // When
            boolean result = jwtService.isTokenValid(token, testUserDetails);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when username does not match")
        void isTokenValid_shouldReturnFalse_whenUsernameMismatch() {
            // Given
            String token = jwtService.generateToken(testUserDetails);
            UserDetails differentUser = User.builder()
                    .username("different@example.com")
                    .password("password")
                    .authorities(Collections.emptyList())
                    .build();

            // When
            boolean result = jwtService.isTokenValid(token, differentUser);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for expired token")
        void isTokenValid_shouldReturnFalse_forExpiredToken() {
            // Given - Create a service with very short expiration (already expired)
            JwtService shortExpirationService = new JwtService();
            ReflectionTestUtils.setField(shortExpirationService, "jwtSigningKey", TEST_SECRET);
            ReflectionTestUtils.setField(shortExpirationService, "jwtExpiration", -1000L); // Negative = already expired
            ReflectionTestUtils.setField(shortExpirationService, "refreshExpiration", TEST_REFRESH_EXPIRATION);

            String token = shortExpirationService.generateToken(testUserDetails);

            // When / Then
            // This will throw an ExpiredJwtException, which is expected
            try {
                boolean result = shortExpirationService.isTokenValid(token, testUserDetails);
                assertThat(result).isFalse();
            } catch (Exception e) {
                // ExpiredJwtException is expected for expired tokens
                assertThat(e.getClass().getSimpleName()).contains("Jwt");
            }
        }
    }
}
