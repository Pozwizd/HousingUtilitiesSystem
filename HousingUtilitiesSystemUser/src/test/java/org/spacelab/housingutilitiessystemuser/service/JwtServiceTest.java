package org.spacelab.housingutilitiessystemuser.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemuser.security.CustomOidcUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private CustomOidcUser customOidcUser;

    @Mock
    private Authentication authentication;

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long JWT_EXPIRATION = 900000;
    private static final long REFRESH_EXPIRATION = 604800000;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);
    }

    @Nested
    @DisplayName("Token Generation")
    class TokenGeneration {
        @Test
        @DisplayName("Should generate token from UserDetails")
        void generateToken_fromUserDetails_shouldGenerate() {
            when(userDetails.getUsername()).thenReturn("testuser");

            String token = jwtService.generateToken(userDetails);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
        }

        @Test
        @DisplayName("Should generate token from CustomOidcUser")
        void generateToken_fromCustomOidcUser_shouldGenerate() {
            when(customOidcUser.getUsername()).thenReturn("testuser");
            when(customOidcUser.getEmail()).thenReturn("test@test.com");

            String token = jwtService.generateToken((UserDetails) customOidcUser);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
        }

        @Test
        @DisplayName("Should generate token from Authentication")
        void generateToken_fromAuthentication_shouldGenerate() {
            when(authentication.getName()).thenReturn("testuser");
            when(authentication.getPrincipal()).thenReturn(userDetails);

            String token = jwtService.generateToken(authentication);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
        }

        @Test
        @DisplayName("Should generate token from Authentication with CustomOidcUser")
        void generateToken_fromAuthenticationWithCustomOidcUser_shouldGenerate() {
            when(authentication.getName()).thenReturn("testuser");
            when(authentication.getPrincipal()).thenReturn(customOidcUser);
            when(customOidcUser.getEmail()).thenReturn("test@test.com");

            String token = jwtService.generateToken(authentication);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
        }

        @Test
        @DisplayName("Should generate refresh token from Authentication")
        void generateRefreshToken_fromAuthentication_shouldGenerate() {
            when(authentication.getName()).thenReturn("testuser");

            String token = jwtService.generateRefreshToken(authentication);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
        }

        @Test
        @DisplayName("Should generate refresh token from UserDetails")
        void generateRefreshToken_fromUserDetails_shouldGenerate() {
            when(userDetails.getUsername()).thenReturn("testuser");

            String token = jwtService.generateRefreshToken(userDetails);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Token Extraction")
    class TokenExtraction {
        @Test
        @DisplayName("Should extract username from token")
        void extractUserName_shouldExtractUsername() {
            when(userDetails.getUsername()).thenReturn("testuser");
            String token = jwtService.generateToken(userDetails);

            String username = jwtService.extractUserName(token);

            assertThat(username).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {
        @Test
        @DisplayName("Should return true for valid token")
        void isTokenValid_shouldReturnTrue_forValidToken() {
            when(userDetails.getUsername()).thenReturn("testuser");
            String token = jwtService.generateToken(userDetails);

            boolean isValid = jwtService.isTokenValid(token, userDetails);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should return false for wrong user")
        void isTokenValid_shouldReturnFalse_forWrongUser() {
            when(userDetails.getUsername()).thenReturn("testuser");
            String token = jwtService.generateToken(userDetails);

            // Change username for validation
            when(userDetails.getUsername()).thenReturn("differentuser");

            boolean isValid = jwtService.isTokenValid(token, userDetails);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should handle expired token gracefully")
        void isTokenValid_shouldHandleExpiredToken() {
            // Create an expired token manually
            String expiredToken = Jwts.builder()
                    .subject("testuser")
                    .issuedAt(new Date(System.currentTimeMillis() - 1000000))
                    .expiration(new Date(System.currentTimeMillis() - 500000))
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                    .compact();

            // Expired token should throw an exception when validated
            try {
                boolean isValid = jwtService.isTokenValid(expiredToken, userDetails);
                assertThat(isValid).isFalse();
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Expected behavior - token is expired
                assertThat(e).isNotNull();
            }
        }
    }
}
