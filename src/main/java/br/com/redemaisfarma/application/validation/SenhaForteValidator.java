/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.ConstraintValidator
 *  jakarta.validation.ConstraintValidatorContext
 */
package br.com.redemaisfarma.application.validation;

import br.com.redemaisfarma.adapters.inbound.web.validator.annotation.SenhaForte;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaForteValidator
implements ConstraintValidator<SenhaForte, String> {
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        return senha != null && senha.length() >= 8 && senha.matches(".*\\d.*");
    }
}

