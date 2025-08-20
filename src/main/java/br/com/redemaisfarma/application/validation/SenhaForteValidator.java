package br.com.redemaisfarma.application.validation;

import br.com.redemaisfarma.application.validation.annotation.SenhaForte;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaForteValidator implements ConstraintValidator<SenhaForte, String> {

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        return senha != null && senha.length() >= 8 && senha.matches(".*\\d.*");
    }
}

