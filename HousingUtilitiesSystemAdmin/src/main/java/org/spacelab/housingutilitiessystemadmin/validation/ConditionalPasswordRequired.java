package org.spacelab.housingutilitiessystemadmin.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalPasswordRequiredValidator.class)
@Documented
public @interface ConditionalPasswordRequired {
    String message() default "users.validation.password.NotBlank";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
