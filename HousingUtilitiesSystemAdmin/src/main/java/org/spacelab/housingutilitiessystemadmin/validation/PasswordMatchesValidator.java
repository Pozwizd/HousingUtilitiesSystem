package org.spacelab.housingutilitiessystemadmin.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.spacelab.housingutilitiessystemadmin.models.user.UserRequest;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserRequest> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(UserRequest userRequest, ConstraintValidatorContext context) {
        if (userRequest == null) {
            return true;
        }

        String password = userRequest.getPassword();
        String confirmPassword = userRequest.getConfirmPassword();

        // Если оба поля пустые, считаем валидацию пройденной
        // (ConditionalPasswordRequiredValidator проверит, нужен ли пароль)
        if ((password == null || password.trim().isEmpty()) && 
            (confirmPassword == null || confirmPassword.trim().isEmpty())) {
            return true;
        }

        // Если хотя бы одно поле заполнено, проверяем совпадение
        boolean isValid = password != null && password.equals(confirmPassword);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("users.validation.password.notMatch")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
