package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de requisição para criação/atualização de produto. - Validações de domínio - Campos de auditoria somente leitura
 * (servidor preenche) - Compatível com MapStruct e OpenAPI
 */
@Schema(name = "ProdutoRequestDTO", description = "Dados para criação/atualização de produto")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProdutoRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID público (UUID) do produto", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("produtoId")
    private UUID produtoId;

    @Schema(description = "SKU do produto", example = "PRD-001-ABC", required = true)
    @NotBlank(message = "{produto.sku.notBlank}")
    @Size(max = 50, message = "{produto.sku.size}")
    @JsonProperty("sku")
    private String sku;

    @Schema(description = "Nome do produto", example = "Dipirona 500mg", required = true)
    @NotBlank(message = "{produto.nome.notBlank}")
    @Size(max = 100, message = "{produto.nome.size}")
    @JsonProperty("nome")
    private String nome;

    @Schema(description = "Descrição detalhada do produto", example = "Analgésico e antitérmico", required = true)
    @NotBlank(message = "{produto.descricao.notBlank}")
    @Size(max = 1000, message = "{produto.descricao.size}")
    @JsonProperty("descricao")
    private String descricao;

    @Schema(description = "Preço unitário do produto", example = "12.50", required = true)
    @NotNull(message = "{produto.preco.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{produto.preco.min}")
    @JsonProperty("preco")
    private BigDecimal preco;

    @Schema(description = "URL da imagem do produto", example = "https://cdn.farma/img/produto1.jpg", required = true)
    @NotBlank(message = "{produto.imagem.notBlank}")
    @Size(max = 200, message = "{produto.imagem.size}")
    @Pattern(regexp = "^(http|https)://.*$", message = "{produto.imagem.pattern}")
    @JsonProperty("imagem")
    private String imagem;

    @Schema(description = "Categoria do produto", example = "ANALGESICO", required = true)
    @NotBlank(message = "{produto.categoria.notBlank}")
    @Size(max = 50, message = "{produto.categoria.size}")
    @JsonProperty("categoria")
    private String categoria;

    @Schema(description = "Código de barras (EAN/UPC)", example = "7891234567895")
    @Size(max = 50, message = "{produto.codigoBarras.size}") // alinha com coluna do banco
    @Pattern(regexp = "\\d{8,13}", message = "{produto.codigoBarras.pattern}")
    @JsonProperty("codigoBarras")
    private String codigoBarras;

    @Schema(description = "Quantidade em estoque", example = "100", required = true)
    @NotNull(message = "{produto.estoque.notNull}")
    @Min(value = 0, message = "{produto.estoque.min}")
    @JsonProperty("estoque")
    private Integer estoque;

    @Schema(description = "Peso do produto (g)", example = "200")
    @Min(value = 1, message = "{produto.peso.min}")
    @JsonProperty("peso")
    private Integer peso;

    @Schema(description = "Dimensões (CxLxA em cm), formato '10,5,2'", example = "10,5,2")
    @Pattern(regexp = "^\\d{1,4}(,\\d{1,4}){2}$", message = "{produto.dimensoes.pattern}")
    @JsonProperty("dimensoes")
    private String dimensoes;

    @Schema(description = "Produto ativo para venda", example = "true")
    @JsonProperty("ativo")
    private Boolean ativo = Boolean.TRUE;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{produto.tenantId.notBlank}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Data/hora de criação", type = "string", format = "date-time", example = "2025-07-04T10:00:00")
    @PastOrPresent(message = "{produto.criadoEm.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value = "criadoEm", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime criadoEm;

    @Schema(description = "Data/hora da última atualização", type = "string", format = "date-time", example = "2025-07-05T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value = "atualizadoEm", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime atualizadoEm;

    // ----------------------------------------------------
    // Construtores
    // ----------------------------------------------------
    public ProdutoRequestDTO() {
    }

    public ProdutoRequestDTO(UUID produtoId, String sku, String nome, String descricao, BigDecimal preco, String imagem,
            String categoria, String codigoBarras, Integer estoque, Integer peso, String dimensoes, Boolean ativo,
            String tenantId, UUID traceId, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.produtoId = produtoId;
        this.sku = sku;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.imagem = imagem;
        this.categoria = categoria;
        this.codigoBarras = codigoBarras;
        this.estoque = estoque;
        this.peso = peso;
        this.dimensoes = dimensoes;
        this.ativo = ativo;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    // ----------------------------------------------------
    // Getters/Setters
    // ----------------------------------------------------
    public UUID getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(UUID produtoId) {
        this.produtoId = produtoId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public Integer getPeso() {
        return peso;
    }

    public void setPeso(Integer peso) {
        this.peso = peso;
    }

    public String getDimensoes() {
        return dimensoes;
    }

    public void setDimensoes(String dimensoes) {
        this.dimensoes = dimensoes;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    // ----------------------------------------------------
    // equals/hashCode/toString
    // ----------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProdutoRequestDTO that))
            return false;
        return Objects.equals(produtoId, that.produtoId) && Objects.equals(sku, that.sku)
                && Objects.equals(nome, that.nome) && Objects.equals(descricao, that.descricao)
                && Objects.equals(preco, that.preco) && Objects.equals(imagem, that.imagem)
                && Objects.equals(categoria, that.categoria) && Objects.equals(codigoBarras, that.codigoBarras)
                && Objects.equals(estoque, that.estoque) && Objects.equals(peso, that.peso)
                && Objects.equals(dimensoes, that.dimensoes) && Objects.equals(ativo, that.ativo)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId)
                && Objects.equals(criadoEm, that.criadoEm) && Objects.equals(atualizadoEm, that.atualizadoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId, sku, nome, descricao, preco, imagem, categoria, codigoBarras, estoque, peso,
                dimensoes, ativo, tenantId, traceId, criadoEm, atualizadoEm);
    }

    @Override
    public String toString() {
        return "ProdutoRequestDTO{" + "produtoId=" + produtoId + ", sku='" + sku + '\'' + ", nome='" + nome + '\''
                + ", descricao='" + descricao + '\'' + ", preco=" + preco + ", imagem='" + imagem + '\''
                + ", categoria='" + categoria + '\'' + ", codigoBarras='" + codigoBarras + '\'' + ", estoque=" + estoque
                + ", peso=" + peso + ", dimensoes='" + dimensoes + '\'' + ", ativo=" + ativo + ", tenantId='" + tenantId
                + '\'' + ", traceId=" + traceId + ", criadoEm=" + criadoEm + ", atualizadoEm=" + atualizadoEm + '}';
    }
}
