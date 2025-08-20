package br.com.redemaisfarma.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Produto {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal precoVenda;
    private String imagem;
    private String categoria;
    private String codigoBarras;

    private BigDecimal precoCusto;
    private Integer estoque;
    private Boolean disponivel;
    private String fabricante;
    private String codigoOriginal;
    private String unidade;
    private LocalDateTime dataCadastro;

    public Produto() {
    }

    public Produto(Long id, String nome, String descricao, BigDecimal precoVenda, String imagem, String categoria,
            String codigoBarras, BigDecimal precoCusto, Integer estoque, Boolean disponivel, String fabricante,
            String codigoOriginal, String unidade, LocalDateTime dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoVenda = precoVenda;
        this.imagem = imagem;
        this.categoria = categoria;
        this.codigoBarras = codigoBarras;
        this.precoCusto = precoCusto;
        this.estoque = estoque;
        this.disponivel = disponivel;
        this.fabricante = fabricante;
        this.codigoOriginal = codigoOriginal;
        this.unidade = unidade;
        this.dataCadastro = dataCadastro;
    }

    // Getters e Setters abaixo (iguais aos anteriores)

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
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

    public BigDecimal getPrecoCusto() {
        return precoCusto;
    }

    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Boolean getDisponivel() {
        return disponivel;
    }

    public void setDisponivel(Boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getCodigoOriginal() {
        return codigoOriginal;
    }

    public void setCodigoOriginal(String codigoOriginal) {
        this.codigoOriginal = codigoOriginal;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public void aplicarDesconto(BigDecimal percentual) {
        if (percentual == null || percentual.compareTo(BigDecimal.ZERO) <= 0
                || percentual.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Percentual de desconto inválido: " + percentual);
        }
        this.precoVenda = this.precoVenda.multiply(BigDecimal.ONE.subtract(percentual));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Produto produto = (Produto) o;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Produto{" + "id=" + id + ", nome='" + nome + '\'' + ", descricao='" + descricao + '\'' + ", precoVenda="
                + precoVenda + ", imagem='" + imagem + '\'' + ", categoria='" + categoria + '\'' + ", codigoBarras='"
                + codigoBarras + '\'' + ", precoCusto=" + precoCusto + ", estoque=" + estoque + ", disponivel="
                + disponivel + ", fabricante='" + fabricante + '\'' + ", codigoOriginal='" + codigoOriginal + '\''
                + ", unidade='" + unidade + '\'' + ", dataCadastro=" + dataCadastro + '}';
    }
}
