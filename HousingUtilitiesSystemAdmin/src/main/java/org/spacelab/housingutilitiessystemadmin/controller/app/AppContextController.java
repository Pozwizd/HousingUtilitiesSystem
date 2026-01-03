package org.spacelab.housingutilitiessystemadmin.controller.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class AppContextController {

    @Value("${spring.application.name}")
    private String appName;

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("appName", appName);

    }

    @ModelAttribute
    public void addCommonAttributes(Model model, Principal principal) {
        // Добавляем principal в модель только если он доступен
        if (principal != null) {
            model.addAttribute("currentUser", principal.getName());
            System.out.println("Principal: " + principal.getName());
        }
        // Не выводим ошибку если principal отсутствует - это нормально для async запросов
    }
}
