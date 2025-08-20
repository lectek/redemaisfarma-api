package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * Informações básicas do atendente (response).
 */
@Schema(name = "AtendenteDTO", description = "Informações básicas do atendente")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AtendenteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do atendente", example = "10")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Nome do atendente", example = "Carlos Souza")
    @JsonProperty("nome")
    private String nome;

    // Construtores
    public AtendenteDTO() {
    }

    public AtendenteDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

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

    // equals/hashCode/toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AtendenteDTO))
            return false;
        AtendenteDTO that = (AtendenteDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome);
    }

    @Override
    public String toString() {
        return "AtendenteDTO{" + "id=" + id + ", nome='" + nome + '\'' + '}';
    }
}
