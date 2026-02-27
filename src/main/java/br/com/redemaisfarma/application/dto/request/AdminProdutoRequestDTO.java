package br.com.redemaisfarma.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class AdminProdutoRequestDTO {

    @NotBlank(message = "{produto.nome.notBlank}")
    @Size(max = 100, message = "{produto.nome.size}")
    private String nome;

    @Size(max = 1000, message = "{produto.descricao.size}")
    private String descricao;

    @NotNull(message = "{produto.preco.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{produto.preco.min}")
    private BigDecimal preco;

    @Size(max = 200, message = "{produto.imagem.size}")
    @Pattern(regexp = "(^$)|(^https?://.*$)", message = "{produto.imagem.pattern}")
    private String imagem;

    @NotBlank(message = "{produto.categoria.notBlank}")
    @Size(max = 50, message = "{produto.categoria.size}")
    private String categoria;

    @Size(max = 50, message = "{produto.codigoBarras.size}")
    @Pattern(regexp = "(^$)|(\\d{8,13})", message = "{produto.codigoBarras.pattern}")
    private String codigoBarras;

    @NotNull(message = "{produto.estoque.notNull}")
    @Min(value = 0, message = "{produto.estoque.min}")
    private Integer estoque;

    private Boolean ativo = Boolean.FALSE;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
