package br.com.redemaisfarma.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import br.com.redemaisfarma.application.validation.annotation.EmailUnico;

import java.util.Arrays;
import java.util.List;

/**
 * ImplementaÃƒÂ§ÃƒÂ£o da lÃƒÂ³gica de verificaÃƒÂ§ÃƒÂ£o de e-mail ÃƒÂºnico. Por enquanto, faz uma simulaÃƒÂ§ÃƒÂ£o local. Futuramente integrarÃƒÂ¡
 * com o banco.
 */
@Component
public class EmailUnicoValidator implements ConstraintValidator<EmailUnico, String> {

    // SimulaÃƒÂ§ÃƒÂ£o de e-mails jÃƒÂ¡ cadastrados (em produÃƒÂ§ÃƒÂ£o virÃƒÂ¡ do banco de dados)
    private static final List<String> EMAILS_CADASTRADOS = Arrays.asList("teste@exemplo.com", "admin@embalando.com",
            "cliente@loja.com");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null)
            return true;

        return !EMAILS_CADASTRADOS.contains(email.trim().toLowerCase());
    }
}

