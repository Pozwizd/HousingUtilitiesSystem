package org.spacelab.housingutilitiessystemuser.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.security.CustomAuthenticationSuccessHandler;
import org.spacelab.housingutilitiessystemuser.security.CustomOidcUserService;
import org.spacelab.housingutilitiessystemuser.security.JwtAuthenticationFilter;
import org.spacelab.housingutilitiessystemuser.security.UserUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

        private final UserUserDetailsService userUserDetailsService;
        private final CustomOidcUserService customOidcUserService;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomAuthenticationSuccessHandler successHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(request -> {
                                        CorsConfiguration corsConfiguration = new CorsConfiguration();
                                        corsConfiguration.setAllowedOriginPatterns(List.of("*"));
                                        corsConfiguration.setAllowedMethods(
                                                        List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                                        corsConfiguration.setAllowedHeaders(List.of("*"));
                                        corsConfiguration.setAllowCredentials(true);
                                        return corsConfiguration;
                                }))
                                .securityContext(context -> context
                                                .securityContextRepository(securityContextRepository()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests((authorize) -> authorize
                                                .requestMatchers("/login", "/perform-login", "/assets/**", "/css/**",
                                                                "/js/**",
                                                                "/assetsArchitect/**")
                                                .permitAll()
                                                .requestMatchers("/.well-known/**").permitAll()
                                                .requestMatchers("/forgotPassword", "/resetPassword", "/changePassword",
                                                                "/confirmation",
                                                                "/success", "/oauth2/**", "/user-info", "/current-user",
                                                                "/debug/**")
                                                .permitAll()
                                                .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**")
                                                .permitAll()
                                                .anyRequest().authenticated())

                                .formLogin((form) -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/perform-login")
                                                .usernameParameter("login")
                                                .passwordParameter("password")
                                                .successHandler(successHandler)
                                                .failureUrl("/login?error=true")
                                                .permitAll())

                                .oauth2Login((oauth2) -> oauth2
                                                .loginPage("/login")
                                                .userInfoEndpoint((userInfo) -> userInfo
                                                                .oidcUserService(customOidcUserService))
                                                .successHandler(successHandler))
                                .logout((logout) -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .deleteCookies("access-token", "refresh-token", "JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .permitAll())

                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
                AuthenticationManagerBuilder authenticationManagerBuilder = http
                                .getSharedObject(AuthenticationManagerBuilder.class);
                authenticationManagerBuilder
                                .userDetailsService(userUserDetailsService)
                                .passwordEncoder(passwordEncoder());
                return authenticationManagerBuilder.build();
        }

        @Bean
        public static PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityContextRepository securityContextRepository() {
                return new DelegatingSecurityContextRepository(
                                new RequestAttributeSecurityContextRepository(),
                                new HttpSessionSecurityContextRepository());
        }
}
