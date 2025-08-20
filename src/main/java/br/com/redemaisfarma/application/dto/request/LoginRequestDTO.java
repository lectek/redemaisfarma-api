package br.com.redemaisfarma.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * FormulÃƒÂ¡rio de login usado na autenticaÃƒÂ§ÃƒÂ£o via pÃƒÂ¡gina HTML.
 *
 * ContÃƒÂ©m campos mÃƒÂ­nimos e validaÃƒÂ§ÃƒÂµes simples para uso seguro em tela.
 */
public class LoginRequestDTO {

    @NotBlank(message = "{login.usuario.notBlank}")
    @Email(message = "{login.usuario.email}")
    @Size(max = 150, message = "{login.usuario.size}")
    private String usuario;

    @NotBlank(message = "{login.senha.notBlank}")
    @Size(min = 8, max = 128, message = "{login.senha.size}")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$", message = "{login.senha.pattern}")
    private String senha;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}

