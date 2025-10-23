package br.com.redemaisfarma.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank(message = "Informe seu e-mail ou CPF.")
    private String emailOuCpf;

    public String getEmailOuCpf() { return emailOuCpf; }
    public void setEmailOuCpf(String emailOuCpf) { this.emailOuCpf = emailOuCpf; }
}
