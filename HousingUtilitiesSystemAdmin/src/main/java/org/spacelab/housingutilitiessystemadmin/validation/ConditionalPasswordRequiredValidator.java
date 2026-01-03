package org.spacelab.housingutilitiessystemadmin.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.models.user.UserRequest;
import org.spacelab.housingutilitiessystemadmin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ConditionalPasswordRequiredValidator implements ConstraintValidator<ConditionalPasswordRequired, UserRequest> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void initialize(ConditionalPasswordRequired constraintAnnotation) {
    }

    @Override
    public boolean isValid(UserRequest userRequest, ConstraintValidatorContext context) {
        if (userRequest == null) {
            return true;
        }

        String password = userRequest.getPassword();
        String confirmPassword = userRequest.getConfirmPassword();
        
        // Проверяем, заполнены ли поля паролей
        boolean passwordsEmpty = (password == null || password.trim().isEmpty()) && 
                                 (confirmPassword == null || confirmPassword.trim().isEmpty());

        log.debug("Validating password. UserId: {}, passwordsEmpty: {}", userRequest.getId(), passwordsEmpty);

        // Если пароли пустые и есть ID пользователя
        if (passwordsEmpty && userRequest.getId() != null) {
            // Проверяем, есть ли у пользователя пароль в БД
            User existingUser = userRepository.findById(userRequest.getId()).orElse(null);
            
            log.debug("Found existing user: {}, has password: {}", 
                existingUser != null, 
                existingUser != null && existingUser.getPassword() != null && !existingUser.getPassword().isEmpty());
            
            if (existingUser != null && existingUser.getPassword() != null && !existingUser.getPassword().isEmpty()) {
                // У пользователя уже есть пароль в БД - валидация OK
                log.debug("User has password in DB - validation passed");
                return true;
            } else {
                // У пользователя нет пароля в БД - пароль обязателен
                log.debug("User has NO password in DB - password required");
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("users.validation.password.NotBlank")
                        .addPropertyNode("password")
                        .addConstraintViolation();
                return false;
            }
        }

        // Если пароли пустые и нет ID (создание нового пользователя)
        if (passwordsEmpty && userRequest.getId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("users.validation.password.NotBlank")
                    .addPropertyNode("password")
                    .addConstraintViolation();
            return false;
        }

        // Если хотя бы одно поле заполнено - оба поля должны быть заполнены
        if (!passwordsEmpty) {
            boolean passwordFilled = password != null && !password.trim().isEmpty();
            boolean confirmPasswordFilled = confirmPassword != null && !confirmPassword.trim().isEmpty();
            
            if (!passwordFilled || !confirmPasswordFilled) {
                context.disableDefaultConstraintViolation();
                if (!passwordFilled) {
                    context.buildConstraintViolationWithTemplate("users.validation.password.NotBlank")
                            .addPropertyNode("password")
                            .addConstraintViolation();
                }
                if (!confirmPasswordFilled) {
                    context.buildConstraintViolationWithTemplate("users.validation.confirmPassword.NotBlank")
                            .addPropertyNode("confirmPassword")
                            .addConstraintViolation();
                }
                return false;
            }
            
            // Проверка минимальной длины пароля
            if (password.length() < 6) {
                log.debug("Password too short: {} characters (minimum 6 required)", password.length());
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("users.validation.password.Size")
                        .addPropertyNode("password")
                        .addConstraintViolation();
                return false;
            }
            
            log.debug("Password validation passed - all checks OK");
        }

        return true;
    }
}
