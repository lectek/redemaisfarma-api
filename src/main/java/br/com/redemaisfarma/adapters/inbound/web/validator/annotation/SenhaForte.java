// src/main/java/br/com/redemaisfarma/adapters/inbound/web/validator/annotation/SenhaForte.java
package br.com.redemaisfarma.adapters.inbound.web.validator.annotation;

import br.com.redemaisfarma.application.validation.SenhaForteValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Valida se uma senha atende critérios de segurança.
 * Padrão: mínimo 8, uma maiúscula, uma minúscula, um número e um caractere especial.
 */
@Documented
@Constraint(validatedBy = SenhaForteValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface SenhaForte {

    String message() default "A senha deve conter: mínimo 8 caracteres, uma maiúscula, uma minúscula, um número e um caractere especial.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // Deixe parametrizável (se precisar flexibilizar no futuro)
    int minLength() default 8;
    boolean requireUppercase() default true;
    boolean requireLowercase() default true;
    boolean requireDigit() default true;
    boolean requireSpecial() default true;
}
