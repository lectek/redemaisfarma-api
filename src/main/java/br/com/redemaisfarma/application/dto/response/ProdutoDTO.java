package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO para exibição resumida das informações de um produto.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ProdutoDTO", description = "Informações resumidas do produto")
public class ProdutoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do produto", example = "1")
    @JsonProperty("produtoId")
    private Long id;

    @Schema(description = "Nome do produto", example = "Dipirona 500mg")
    @JsonProperty("nome")
    private String nome;

    @Schema(description = "Preço do produto", example = "12.99")
    @JsonProperty("preco")
    private BigDecimal preco;

    // ===========================
    // Getters e Setters
    // ===========================

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

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    // ===========================
    // equals e hashCode
    // ===========================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProdutoDTO))
            return false;
        ProdutoDTO that = (ProdutoDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(nome, that.nome) && Objects.equals(preco, that.preco);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, preco);
    }

    // ===========================
    // toString
    // ===========================

    @Override
    public String toString() {
        return "ProdutoDTO{" + "id=" + id + ", nome='" + nome + '\'' + ", preco=" + preco + '}';
    }
}
