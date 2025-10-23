package br.com.redemaisfarma.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "Informe a senha atual.")
    private String senhaAtual;

    @NotBlank(message = "Informe a nova senha.")
    @Size(min = 8, max = 128, message = "A nova senha deve ter entre 8 e 128 caracteres.")
    @Pattern(
        // minúscula, MAIÚSCULA, número e caractere especial
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,128}$",
        message = "A nova senha deve ter minúscula, MAIÚSCULA, número e caractere especial."
    )
    private String novaSenha;

    @NotBlank(message = "Confirme a nova senha.")
    private String confirmarNovaSenha;

    public String getSenhaAtual() { return senhaAtual; }
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }
    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
    public String getConfirmarNovaSenha() { return confirmarNovaSenha; }
    public void setConfirmarNovaSenha(String confirmarNovaSenha) { this.confirmarNovaSenha = confirmarNovaSenha; }
}
