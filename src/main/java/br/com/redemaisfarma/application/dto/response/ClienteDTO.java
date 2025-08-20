package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ClienteDTO", description = "Dados básicos do cliente")
public class ClienteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do cliente", example = "42")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Nome do cliente", example = "João Silva")
    @JsonProperty("nome")
    private String nome;

    @Schema(description = "E-mail do cliente", example = "joao@exemplo.com")
    @JsonProperty("email")
    private String email;

    @Schema(description = "CPF do cliente", example = "12345678901")
    @JsonProperty("cpf")
    private String cpf;

    // Getters/Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClienteDTO))
            return false;
        ClienteDTO that = (ClienteDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
