package br.com.redemaisfarma.application.dto.request;

import br.com.redemaisfarma.application.validation.annotation.EmailUnico;
import br.com.redemaisfarma.application.validation.annotation.SenhaForte;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Representa o formulÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rio de cadastro de cliente. Usado com th:object no HTML para capturar e validar dados vindos da
 * camada web.
 */
public class CadastroClienteRequestDTO {

    @NotBlank(message = "O nome ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â© obrigatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rio.")
    @Size(min = 2, max = 60, message = "O nome deve ter entre 2 e 60 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â© obrigatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rio.")
    @Email(message = "Informe um e-mail vÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡lido.")
    @EmailUnico(message = "Este e-mail jÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ estÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ em uso.")
    private String email;

    @SenhaForte
    private String senha;

    @NotBlank(message = "A confirmaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de senha ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â© obrigatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ria.")
    private String confirmarSenha;

    @NotBlank(message = "O telefone ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â© obrigatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rio.")
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Telefone invÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡lido. Ex: (83) 91234-5678")
    private String telefone;

    // Campo opcional - usado futuramente para segmentar perfis ou personalizar a loja
    private String genero;

    // Campo opcional - usado futuramente para campanhas ou recomendaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes
    private String dataNascimento;

    /**
     * Construtor padrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o.
     */
    public CadastroClienteRequestDTO() {
    }

    // Getters e Setters com validaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o e comentÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rios

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmarSenha() {
        return confirmarSenha;
    }

    public void setConfirmarSenha(String confirmarSenha) {
        this.confirmarSenha = confirmarSenha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    /**
     * Verifica se a senha e a confirmaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o iguais. Pode ser usado no controller ou em um validador customizado.
     */
    public boolean isSenhaConfirmada() {
        return senha != null && senha.equals(confirmarSenha);
    }

    @Override
    public String toString() {
        return "CadastroClienteRequestDTO{" + "nome='" + nome + '\'' + ", email='" + email + '\'' + ", telefone='" + telefone
                + '\'' + ", genero='" + genero + '\'' + ", dataNascimento='" + dataNascimento + '\'' + '}';
    }
}

