// src/main/java/br/com/redemaisfarma/application/validation/SenhaForteValidator.java
package br.com.redemaisfarma.adapters.inbound.web.validator;

import br.com.redemaisfarma.adapters.inbound.web.validator.annotation.SenhaForte;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaForteValidator implements ConstraintValidator<SenhaForte, String> {

    private int minLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecial;

    @Override
    public void initialize(SenhaForte c) {
        this.minLength       = c.minLength();
        this.requireUppercase= c.requireUppercase();
        this.requireLowercase= c.requireLowercase();
        this.requireDigit    = c.requireDigit();
        this.requireSpecial  = c.requireSpecial();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        if (value.length() < minLength) return false;

        if (requireUppercase && value.chars().noneMatch(Character::isUpperCase)) return false;
        if (requireLowercase && value.chars().noneMatch(Character::isLowerCase)) return false;
        if (requireDigit     && value.chars().noneMatch(Character::isDigit))     return false;

        if (requireSpecial) {
            boolean hasSpecial = value.chars().anyMatch(c ->
                    "!@#$%^&*()_+-=[]{}|;':\",.<>/?`~".indexOf(c) >= 0);
            if (!hasSpecial) return false;
        }
        return true;
    }
}

