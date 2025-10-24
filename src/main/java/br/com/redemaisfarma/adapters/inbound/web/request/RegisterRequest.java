/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.adapters.inbound.web.request;

import br.com.redemaisfarma.adapters.inbound.web.validator.annotation.SenhaForte;
import br.com.redemaisfarma.application.validation.annotation.EmailUnico;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank(message="Nome \u00e9 obrigat\u00f3rio") String name, @NotBlank(message="E-mail \u00e9 obrigat\u00f3rio") @Email(message="E-mail inv\u00e1lido") @EmailUnico @NotBlank(message="E-mail \u00e9 obrigat\u00f3rio") @Email(message="E-mail inv\u00e1lido") String email, @NotBlank(message="CPF \u00e9 obrigat\u00f3rio") @Size(min=11, max=14, message="CPF deve ter 11 d\u00edgitos (com ou sem m\u00e1scara)") @Pattern(regexp="\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message="CPF deve ser 11 d\u00edgitos ou no formato 000.000.000-00") @NotBlank(message="CPF \u00e9 obrigat\u00f3rio") @Size(min=11, max=14, message="CPF deve ter 11 d\u00edgitos (com ou sem m\u00e1scara)") @Pattern(regexp="\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message="CPF deve ser 11 d\u00edgitos ou no formato 000.000.000-00") String cpf, @NotBlank(message="Senha \u00e9 obrigat\u00f3ria") @Size(min=8, max=72, message="A senha deve ter entre 8 e 72 caracteres") @SenhaForte @NotBlank(message="Senha \u00e9 obrigat\u00f3ria") @Size(min=8, max=72, message="A senha deve ter entre 8 e 72 caracteres") String password) {
    public String cpfNormalizado() {
        return this.cpf == null ? null : this.cpf.replaceAll("\\D", "");
    }
}

