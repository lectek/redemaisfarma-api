package br.com.redemaisfarma.application.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para informações completas de um produto no sistema RedeMaisFarma.
 *
 * Inclui dados de identificação, estoque, preço, categorização, rastreamento de validade, auditoria e integração com
 * IA.
 */
@Schema(name = "ProdutoResponseDTO", description = "Detalhes do produto cadastrado no sistema")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProdutoResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único do produto", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    @NotNull(message = "{produto.id.notNull}")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "Nome do produto", example = "Dipirona 500mg", required = true)
    @NotBlank(message = "{produto.nome.notBlank}")
    @Size(max = 100, message = "{produto.nome.size}")
    @JsonProperty("nome")
    private String nome;

    @Schema(description = "Descrição do produto", example = "Analgésico e antitérmico", required = true)
    @NotBlank(message = "{produto.descricao.notBlank}")
    @Size(max = 1000, message = "{produto.descricao.size}")
    @JsonProperty("descricao")
    private String descricao;

    @Schema(description = "Preço unitário", example = "12.50", required = true)
    @NotNull(message = "{produto.preco.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{produto.preco.min}")
    @Digits(integer = 12, fraction = 2, message = "{produto.preco.digits}")
    @JsonProperty("preco")
    private BigDecimal preco;

    @Schema(description = "URL da imagem do produto", example = "https://cdn.farma.com.br/img/dipirona.jpg")
    @Size(max = 255, message = "{produto.imagem.size}")
    @JsonProperty("imagem")
    private String imagem;

    @Schema(description = "Categoria do produto", example = "ANALGESICOS", required = true)
    @NotBlank(message = "{produto.categoria.notBlank}")
    @JsonProperty("categoria")
    private String categoria;

    @Schema(description = "Quantidade atual em estoque", example = "150", required = true)
    @NotNull(message = "{produto.estoqueAtual.notNull}")
    @Min(value = 0, message = "{produto.estoqueAtual.min}")
    @JsonProperty("estoqueAtual")
    private Integer estoqueAtual;

    @Schema(description = "Data de validade do produto", type = "string", format = "date", example = "2026-12-31", required = true)
    @NotNull(message = "{produto.validade.notNull}")
    @Future(message = "{produto.validade.future}")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("validade")
    private LocalDate validade;

    @Schema(description = "Código de barras", example = "7896000033445", required = true)
    @NotBlank(message = "{produto.codigoBarras.notBlank}")
    @Pattern(regexp = "^(\\d{8}|\\d{12,14})$", message = "{produto.codigoBarras.pattern}")
    @JsonProperty("codigoBarras")
    private String codigoBarras;

    @Schema(description = "Marca do produto", example = "Genérico")
    @Size(max = 100, message = "{produto.marca.size}")
    @JsonProperty("marca")
    private String marca;

    @Schema(description = "Fornecedor do produto", example = "LekTeC Indústria")
    @Size(max = 100, message = "{produto.fornecedor.size}")
    @JsonProperty("fornecedor")
    private String fornecedor;

    @Schema(description = "Quantidade total vendida", example = "1200")
    @PositiveOrZero(message = "{produto.quantidadeVendida.min}")
    @JsonProperty("quantidadeVendida")
    private Long quantidadeVendida;

    @Schema(description = "Data/hora de cadastro", type = "string", format = "date-time", example = "2025-01-01T08:00:00", required = true)
    @NotNull(message = "{produto.dataCadastro.notNull}")
    @PastOrPresent(message = "{produto.dataCadastro.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataCadastro")
    private LocalDateTime dataCadastro;

    @Schema(description = "Data/hora da última atualização", type = "string", format = "date-time", example = "2025-06-15T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataAtualizacao")
    private LocalDateTime dataAtualizacao;

    @Schema(description = "Flag de produto em destaque", example = "true")
    @JsonProperty("produtoDestaque")
    private Boolean produtoDestaque = Boolean.FALSE;

    @Schema(description = "Flag de recomendação IA", example = "true")
    @JsonProperty("produtoRecomendadoIA")
    private Boolean produtoRecomendadoIA = Boolean.FALSE;

    @Schema(description = "Flag de produto controlado (venda restrita)", example = "false")
    @JsonProperty("produtoControlado")
    private Boolean produtoControlado = Boolean.FALSE;

    @Schema(description = "Avaliação média do produto (0.0 - 5.0)", example = "4.5")
    @DecimalMin(value = "0.0", inclusive = true, message = "{produto.avaliacaoMedia.min}")
    @DecimalMax(value = "5.0", inclusive = true, message = "{produto.avaliacaoMedia.max}")
    @Digits(integer = 1, fraction = 2, message = "{produto.avaliacaoMedia.digits}")
    @JsonProperty("avaliacaoMedia")
    private BigDecimal avaliacaoMedia;

    @Schema(description = "Lista de tags do produto", example = "[\"dor-de-cabeca\", \"antitermico\"]")
    @JsonProperty("tags")
    private List<@NotBlank @Size(max = 50) String> tags;

    @Schema(description = "Situação do produto", example = "ATIVO", required = true, allowableValues = { "ATIVO",
            "INATIVO", "DESCONTINUADO", "ESGOTADO" })
    @NotNull(message = "{produto.situacao.notNull}")
    @JsonProperty("situacao")
    private SituacaoProduto situacao;

    // Construtores
    public ProdutoResponseDTO() {
    }

    public ProdutoResponseDTO(UUID id, String nome, String descricao, BigDecimal preco, String imagem, String categoria,
            Integer estoqueAtual, LocalDate validade, String codigoBarras, String marca, String fornecedor,
            Long quantidadeVendida, LocalDateTime dataCadastro, LocalDateTime dataAtualizacao, Boolean produtoDestaque,
            Boolean produtoRecomendadoIA, Boolean produtoControlado, BigDecimal avaliacaoMedia, List<String> tags,
            SituacaoProduto situacao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.imagem = imagem;
        this.categoria = categoria;
        this.estoqueAtual = estoqueAtual;
        this.validade = validade;
        this.codigoBarras = codigoBarras;
        this.marca = marca;
        this.fornecedor = fornecedor;
        this.quantidadeVendida = quantidadeVendida;
        this.dataCadastro = dataCadastro;
        this.dataAtualizacao = dataAtualizacao;
        this.produtoDestaque = produtoDestaque;
        this.produtoRecomendadoIA = produtoRecomendadoIA;
        this.produtoControlado = produtoControlado;
        this.avaliacaoMedia = avaliacaoMedia;
        this.tags = tags;
        this.situacao = situacao;
    }

    // Getters/Setters (iguais aos seus — mantidos)

    // equals, hashCode, toString (mantidos)

    public enum SituacaoProduto {
        ATIVO, INATIVO, DESCONTINUADO, ESGOTADO
    }
}
