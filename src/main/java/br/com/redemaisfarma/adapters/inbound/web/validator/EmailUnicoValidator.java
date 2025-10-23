// src/main/java/br/com/redemaisfarma/application/validation/EmailUnicoValidator.java
package br.com.redemaisfarma.adapters.inbound.web.validator;

import br.com.redemaisfarma.application.validation.annotation.EmailUnico;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementa a validação de unicidade de e-mail consultando o banco via JPA.
 */
public class EmailUnicoValidator implements ConstraintValidator<EmailUnico, String> {

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return !usuarioJpaRepository.existsByEmail(email);
    }
}
