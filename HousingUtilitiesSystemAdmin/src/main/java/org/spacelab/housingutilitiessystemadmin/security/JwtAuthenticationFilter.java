package org.spacelab.housingutilitiessystemadmin.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.service.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    public static final String ACCESS_TOKEN_COOKIE = "access-token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh-token";
    private static final String SECURITY_CONTEXT_ATTR = "SAVED_SECURITY_CONTEXT";

    private final JwtService jwtService;
    private final AdminUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/") ||
                path.equals("/login") ||
                path.equals("/register") ||
                path.equals("/forgotPassword") ||
                path.equals("/confirmation") ||
                path.startsWith("/oauth2/authorization/") ||
                path.startsWith("/login/oauth2/code/") ||
                path.startsWith("/assets/") ||
                path.startsWith("/css/") ||
                path.startsWith("/.well-known/") ||
                path.startsWith("/js/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        DispatcherType dispatcherType = request.getDispatcherType();

        // ASYNC dispatch - восстанавливаем из атрибутов
        if (dispatcherType == DispatcherType.ASYNC) {
            SecurityContext savedContext = (SecurityContext) request.getAttribute(SECURITY_CONTEXT_ATTR);
            if (savedContext != null) {
                SecurityContextHolder.setContext(savedContext);
                log.debug("ASYNC dispatch: SecurityContext восстановлен");
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Всегда очищаем SecurityContext в начале REQUEST
        SecurityContextHolder.clearContext();

        String accessToken = extractToken(request);

        // ✅ НОВАЯ ЛОГИКА: Если access-token нет, но есть refresh-token - создаем новый access-token
        if (accessToken == null) {
            log.debug("Access token не найден, проверяем refresh token");

            boolean refreshed = tryCreateAccessTokenFromRefresh(request, response);

            if (refreshed) {
                log.info("✅ Access token успешно создан из refresh token");
                // После создания нового access-token продолжаем выполнение
                // SecurityContext уже установлен в tryCreateAccessTokenFromRefresh
            } else {
                log.debug("❌ Refresh token отсутствует или невалиден - пользователь НЕаутентифицирован");
                filterChain.doFilter(request, response);
                return;
            }
        } else {
            // Access token присутствует - валидируем его
            try {
                var username = jwtService.extractUserName(accessToken);

                if (StringUtils.hasText(username)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.isTokenValid(accessToken, userDetails)) {
                        authenticateUser(userDetails, request);
                        log.debug("✅ Пользователь {} аутентифицирован через access token", username);
                    } else {
                        log.debug("⚠️ Access token невалиден, пробуем refresh");
                        tryRefreshAccessToken(request, response, userDetails);
                    }
                }
            } catch (Exception e) {
                log.error("❌ Ошибка при обработке access token: {}", e.getMessage());
                // Пробуем создать новый из refresh
                tryCreateAccessTokenFromRefresh(request, response);
            }
        }

        // Сохраняем SecurityContext для ASYNC dispatch
        SecurityContext currentContext = SecurityContextHolder.getContext();
        if (currentContext != null && currentContext.getAuthentication() != null) {
            request.setAttribute(SECURITY_CONTEXT_ATTR, currentContext);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Попытка создать новый access token из refresh token
     *
     * @return true если access token успешно создан, false если refresh token отсутствует/невалиден
     */
    private boolean tryCreateAccessTokenFromRefresh(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> refreshToken = getCookieValue(request, REFRESH_TOKEN_COOKIE);

        if (refreshToken.isEmpty()) {
            log.debug("Refresh token не найден");
            return false;
        }

        try {
            String username = jwtService.extractUserName(refreshToken.get());

            if (!StringUtils.hasText(username)) {
                log.warn("Username не извлечен из refresh token");
                return false;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(refreshToken.get(), userDetails)) {
                // Создаем новый access token
                String newAccessToken = jwtService.generateToken(userDetails);

                // Отправляем новый access token в cookie
                Cookie accessCookie = createAccessTokenCookie(newAccessToken);
                response.addCookie(accessCookie);

                // Аутентифицируем пользователя
                authenticateUser(userDetails, request);

                log.info("✅ Новый access token создан для пользователя: {}", username);
                return true;
            } else {
                log.warn("⚠️ Refresh token невалиден для пользователя: {}", username);
                return false;
            }
        } catch (Exception e) {
            log.error("❌ Ошибка при создании access token из refresh: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Обновление access token когда текущий невалиден
     */
    private void tryRefreshAccessToken(HttpServletRequest request, HttpServletResponse response, UserDetails userDetails) {
        Optional<String> refreshToken = getCookieValue(request, REFRESH_TOKEN_COOKIE);

        if (refreshToken.isPresent() && jwtService.isTokenValid(refreshToken.get(), userDetails)) {
            String newAccessToken = jwtService.generateToken(userDetails);

            Cookie accessCookie = createAccessTokenCookie(newAccessToken);
            response.addCookie(accessCookie);

            authenticateUser(userDetails, request);

            log.info("✅ Access token обновлен для пользователя: {}", userDetails.getUsername());
        } else {
            log.warn("⚠️ Не удалось обновить access token - refresh token отсутствует или невалиден");
        }
    }

    private void authenticateUser(UserDetails userDetails, HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }

    private Cookie createAccessTokenCookie(String token) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // В продакшене установите true с HTTPS!
        cookie.setPath("/");
        cookie.setMaxAge(15 * 60); // 15 минут
        return cookie;
    }

    private String extractToken(HttpServletRequest request) {
        // Сначала проверяем Authorization header
        var authHeader = request.getHeader(HEADER_NAME);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        // Затем проверяем cookie
        Optional<String> tokenFromCookie = getCookieValue(request, ACCESS_TOKEN_COOKIE);
        return tokenFromCookie.orElse(null);
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
