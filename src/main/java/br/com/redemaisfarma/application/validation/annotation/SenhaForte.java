package br.com.redemaisfarma.application.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = br.com.redemaisfarma.application.validation.SenhaForteValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaForte {

    String message() default "A senha deve ter pelo menos 8 caracteres e conter ao menos um nÃƒÂºmero.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

