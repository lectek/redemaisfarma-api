/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import br.com.redemaisfarma.adapters.inbound.web.validator.annotation.SenhaForte;
import br.com.redemaisfarma.application.validation.annotation.EmailUnico;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CadastroClienteRequestDTO {
    @NotBlank(message="O nome \u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a9 obrigat\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00b3rio.")
    @Size(min=2, max=60, message="O nome deve ter entre 2 e 60 caracteres.")
    private @NotBlank(message="O nome \u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a9 obrigat\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00b3rio.") @Size(min=2, max=60, message="O nome deve ter entre 2 e 60 caracteres.") String nome;
    @NotBlank(message="O e-mail \u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a9 obrigat\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00b3rio.")
    @Email(message="Informe um e-mail v\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a1lido.")
    @EmailUnico(message="Este e-mail j\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a1 est\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a1 em uso.")
    private @NotBlank(message="O e-mail \u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a9 obrigat\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00b3rio.") @Email(message="Informe um e-mail v\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a1lido.") String email;
    @SenhaForte
    private String senha;
    @NotBlank(message="A confirma\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a7\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a3o de senha \u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a9 obrigat\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00b3ria.")
    private @NotBlank(message="A confirma\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a7\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a3o de senha \u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a9 obrigat\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00b3ria.") String confirmarSenha;
    @NotBlank(message="O telefone \u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a9 obrigat\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00b3rio.")
    @Pattern(regexp="\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message="Telefone inv\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a1lido. Ex: (83) 91234-5678")
    private @NotBlank(message="O telefone \u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a9 obrigat\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00b3rio.") @Pattern(regexp="\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message="Telefone inv\u00c3\u0192\u00c6\u2019\u00c3\u2020\u00e2\u20ac\u2122\u00c3\u0192\u00e2\u20ac\u0161\u00c3\u201a\u00c2\u00a1lido. Ex: (83) 91234-5678") String telefone;
    private String genero;
    private String dataNascimento;

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmarSenha() {
        return this.confirmarSenha;
    }

    public void setConfirmarSenha(String confirmarSenha) {
        this.confirmarSenha = confirmarSenha;
    }

    public String getTelefone() {
        return this.telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getGenero() {
        return this.genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDataNascimento() {
        return this.dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public boolean isSenhaConfirmada() {
        return this.senha != null && this.senha.equals(this.confirmarSenha);
    }

    public String toString() {
        return "CadastroClienteRequestDTO{nome='" + this.nome + "', email='" + this.email + "', telefone='" + this.telefone + "', genero='" + this.genero + "', dataNascimento='" + this.dataNascimento + "'}";
    }
}

