package org.spacelab.housingutilitiessystemadmin.controller.app;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.Admin;
import org.spacelab.housingutilitiessystemadmin.service.AdminService;
import org.spacelab.housingutilitiessystemadmin.service.JavaMailService;
import org.spacelab.housingutilitiessystemadmin.service.JwtService;
import org.spacelab.housingutilitiessystemadmin.service.PasswordResetTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Slf4j
@Controller
@AllArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final JavaMailService mailService;
    private final AdminService adminService;
    private final SecurityContextRepository securityContextRepository;

    @GetMapping("/login")
    public ModelAndView login(Model model) {
        model.addAttribute("title", "Login");
        return new ModelAndView("auth/auth-login-cover");
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        log.debug("showRegisterPage() - Showing registration page");
        return "auth/auth-register-cover";
    }

    @PostMapping("/register")
    public String register(@RequestParam("email") String email,
                          @RequestParam("username") String username,
                          @RequestParam("password") String password,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Model model) {
        log.debug("register() - Registration attempt for email: {}", email);
        
        try {
            // Проверяем, не существует ли уже пользователь с таким email
            Optional<Admin> existingAdmin = adminService.findOptByEmail(email);
            if (existingAdmin.isPresent()) {
                log.warn("register() - Email already exists: {}", email);
                model.addAttribute("error", "Пользователь с таким email уже существует");
                return "auth/auth-register-cover";
            }
            
            // Создаем нового администратора
            Admin admin = adminService.createAdmin(email, password, username);
            log.info("register() - Successfully registered new admin: {}", email);
            
            // Автоматически входим в систему после регистрации
            try {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(email, password);
                Authentication authentication = authenticationManager.authenticate(authToken);
                
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                securityContextRepository.saveContext(context, request, response);
                
                log.info("register() - Auto-login successful for: {}", email);
                return "redirect:/";
                
            } catch (AuthenticationException e) {
                log.warn("register() - Auto-login failed, redirecting to login page", e);
                return "redirect:/login?registered=true";
            }
            
        } catch (Exception e) {
            log.error("register() - Registration failed for email: {}", email, e);
            model.addAttribute("error", "Ошибка при регистрации. Попробуйте еще раз.");
            return "auth/auth-register-cover";
        }
    }

    @GetMapping("/confirmation")
    public String confirmSending(){
        return "auth/auth-confirmation-cover";
    }
    @GetMapping("/forgotPassword")
    public String forgotPassword(){
        return "auth/auth-forgot-password-cover";
    }

    @GetMapping("/changePassword")
    public String changePassword(@RequestParam("token")String token, Model model){
        log.debug("changePassword() - GET request with token: {}", token);
        model.addAttribute("token", token);
        if(passwordResetTokenService.validatePasswordResetToken(token)){
            log.debug("changePassword() - Token is valid, showing change password form");
            return "auth/auth-change-password-cover";
        } else {
            log.warn("changePassword() - Token is invalid or expired, showing expired page");
            return "auth/auth-token-expired-cover";
        }
    }
    @GetMapping("/success")
    public String success(){
        return "auth/auth-password-changed-cover";
    }
    @PostMapping("/changePassword")
    public @ResponseBody String setNewPassword(@RequestParam("token")String token, @RequestParam("password")String password){
        log.debug("setNewPassword() - POST request to change password with token: {}", token);
        if(passwordResetTokenService.validatePasswordResetToken(token)){
            passwordResetTokenService.updatePassword(token,password);
            log.info("setNewPassword() - Password changed successfully");
            return "success";
        } else {
            log.warn("setNewPassword() - Failed to change password, token is invalid");
            return "wrong";
        }
    }
    @PostMapping("/resetPassword")
    public @ResponseBody String resetPassword(HttpServletRequest request, @RequestParam("email")String email){
        log.debug("resetPassword() - Password reset request for email: {}", email);
        if(email.isEmpty()){
            log.warn("resetPassword() - Email is blank");
            return "blank";
        }
        Optional<Admin> admin = adminService.findOptByEmail(email);
        if(admin.isEmpty()){
            log.warn("resetPassword() - Admin not found for email: {}", email);
            return "wrong";
        }
        String token = passwordResetTokenService.createAndSavePasswordResetToken(admin.get());
        mailService.sendToken(token,email,request);
        log.info("resetPassword() - Password reset email sent to: {}", email);
        return "success";
    }



//    @PostMapping("/perform-login")
//    public String performLogin(@RequestParam("email") String email,
//                               @RequestParam("password") String password,
//                               HttpServletRequest request,
//                               HttpServletResponse response,
//                               Model model) {
//        try {
//            // Создаём токен аутентификации
//            UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(email, password);
//
//            // Аутентифицируем через AuthenticationManager
//            Authentication authentication = authenticationManager.authenticate(authToken);
//
//            // Устанавливаем аутентификацию в SecurityContext
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            // Генерируем JWT токены
//            String accessToken = jwtService.generateToken(authentication);
//            String refreshToken = jwtService.generateRefreshToken(authentication);
//
//            // Создаём cookies
//            String contextPath = request.getContextPath();
//            Cookie accessCookie = createAccessTokenCookie(accessToken, contextPath);
//            Cookie refreshCookie = createRefreshTokenCookie(refreshToken, contextPath);
//
//            response.addCookie(accessCookie);
//            response.addCookie(refreshCookie);
//
//            // Перенаправляем на главную страницу
//            return "redirect:/";
//
//        } catch (AuthenticationException e) {
//            model.addAttribute("error", true);
//            model.addAttribute("title", "Login");
//            return "auth/auth-login-cover";
//        }
//    }
//
//    private Cookie createAccessTokenCookie(String token, String contextPath) {
//        Cookie cookie = new Cookie("access-token", token);
//        cookie.setHttpOnly(true);
//        cookie.setSecure(false);
//        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
//        cookie.setMaxAge(15 * 60);
//        return cookie;
//    }
//
//    private Cookie createRefreshTokenCookie(String token, String contextPath) {
//        Cookie cookie = new Cookie("refresh-token", token);
//        cookie.setHttpOnly(true);
//        cookie.setSecure(false);
//        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
//        cookie.setMaxAge(7 * 24 * 60 * 60);
//        return cookie;
//    }
}
