package org.spacelab.housingutilitiessystemadmin.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.service.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String accessToken = jwtService.generateToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);
        
        String contextPath = request.getContextPath();
        Cookie accessCookie = createAccessTokenCookie(accessToken, contextPath);
        Cookie refreshCookie = createRefreshTokenCookie(refreshToken, contextPath);
        
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        String redirectUrl = determineTargetUrl(authentication, contextPath);

        log.info("Пользователь {} получил access и refresh токены и перенаправлен на {}",
                authentication.getName(), redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    private Cookie createAccessTokenCookie(String token, String contextPath) {
        Cookie cookie = new Cookie("access-token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
        cookie.setMaxAge(15 * 60);
        return cookie;
    }

    private Cookie createRefreshTokenCookie(String token, String contextPath) {
        Cookie cookie = new Cookie("refresh-token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
        cookie.setMaxAge(7 * 24 * 60 * 60);
        return cookie;
    }

    protected String determineTargetUrl(Authentication authentication, String contextPath) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();

            switch (role) {
                case "ROLE_ADMIN":
                    return contextPath + "/";
                default:
                    log.warn("Неизвестная роль: {}", role);
            }
        }

        return contextPath + "/";
    }
}
