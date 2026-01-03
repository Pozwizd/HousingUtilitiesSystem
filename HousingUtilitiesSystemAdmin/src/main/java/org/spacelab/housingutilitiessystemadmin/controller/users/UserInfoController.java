package org.spacelab.housingutilitiessystemadmin.controller.users;

import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.security.CustomOidcUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;


@Controller
@Slf4j
public class UserInfoController {

    @GetMapping("/user-info")
    public String userInfo(Model model, Authentication authentication) {
        log.info("=== ADMIN INFO PAGE ===");
        
        if (authentication != null) {
            log.info("Authentication: {}", authentication.getClass().getSimpleName());
            log.info("Principal: {}", authentication.getPrincipal().getClass().getSimpleName());
            log.info("Authorities: {}", authentication.getAuthorities());
            
            model.addAttribute("authentication", authentication);
            model.addAttribute("principal", authentication.getPrincipal());
            model.addAttribute("authorities", authentication.getAuthorities());
            
            if (authentication.getPrincipal() instanceof CustomOidcUser customOidcUser) {
                model.addAttribute("customUser", customOidcUser);
                model.addAttribute("admin", customOidcUser.getAdmin());
                log.info("CustomOidcUser detected: {}", customOidcUser.getEmail());
            }
        }
        
        return "user-info";
    }

    @GetMapping("/current-user")
    @ResponseBody
    public Map<String, Object> getCurrentUser(Authentication authentication, 
                                            @AuthenticationPrincipal OidcUser oidcUser) {
        log.info("=== CURRENT ADMIN API ===");
        
        Map<String, Object> adminInfo = new HashMap<>();
        
        if (authentication != null) {
            adminInfo.put("authenticated", true);
            adminInfo.put("authType", authentication.getClass().getSimpleName());
            adminInfo.put("principalType", authentication.getPrincipal().getClass().getSimpleName());
            adminInfo.put("authorities", authentication.getAuthorities());
            
            if (authentication.getPrincipal() instanceof CustomOidcUser customOidcUser) {
                adminInfo.put("email", customOidcUser.getEmail());
                adminInfo.put("name", customOidcUser.getName());
                adminInfo.put("adminId", customOidcUser.getAdmin().getId());
                adminInfo.put("adminRole", customOidcUser.getAdmin().getRole());
                adminInfo.put("username", customOidcUser.getAdmin().getUsername());
                adminInfo.put("displayName", customOidcUser.getAdmin().getDisplayName());
                adminInfo.put("avatar", customOidcUser.getAdmin().getPathAvatar());
                adminInfo.put("enabled", customOidcUser.getAdmin().isEnabled());
                
                log.info("CustomOidcUser API response: email={}, role={}", 
                    customOidcUser.getEmail(), customOidcUser.getAdmin().getRole());
            }
        } else {
            adminInfo.put("authenticated", false);
        }
        
        return adminInfo;
    }

    @GetMapping("/debug/auth")
    @ResponseBody
    public Map<String, Object> debugAuth(Authentication authentication,
                                       @AuthenticationPrincipal OidcUser oidcUser) {
        log.info("=== DEBUG ADMIN AUTH ===");
        
        Map<String, Object> debug = new HashMap<>();
        
        if (authentication != null) {
            debug.put("authentication", Map.of(
                "class", authentication.getClass().getName(),
                "name", authentication.getName(),
                "authorities", authentication.getAuthorities(),
                "details", authentication.getDetails(),
                "credentials", authentication.getCredentials() != null ? "***" : null
            ));
            
            debug.put("principal", Map.of(
                "class", authentication.getPrincipal().getClass().getName(),
                "toString", authentication.getPrincipal().toString()
            ));
            
            if (oidcUser != null) {
                debug.put("oidcUser", Map.of(
                    "class", oidcUser.getClass().getName(),
                    "name", oidcUser.getName(),
                        "templates/email", oidcUser.getEmail(),
                    "attributes", oidcUser.getAttributes(),
                    "authorities", oidcUser.getAuthorities()
                ));
            }
        }
        
        return debug;
    }
}
