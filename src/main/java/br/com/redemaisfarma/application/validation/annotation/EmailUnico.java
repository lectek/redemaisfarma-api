package br.com.redemaisfarma.application.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import br.com.redemaisfarma.application.validation.EmailUnicoValidator;

/**
 * ValidaÃƒÂ§ÃƒÂ£o personalizada para verificar se um e-mail jÃƒÂ¡ estÃƒÂ¡ em uso. Deve ser usada em campos do tipo String,
 * como @EmailUnico.
 */
@Documented
@Constraint(validatedBy = EmailUnicoValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailUnico {

    String message() default "Este e-mail jÃƒÂ¡ estÃƒÂ¡ cadastrado.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

