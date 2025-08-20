package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa um produto no banco MySQL da farmácia. Inclui informações de estoque, preços, identificação e
 * auditoria.
 */
// imports permanecem os mesmos

@Entity
@Table(name = "produto")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ProdutoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Long id;

    // Novo campo adicional
    @Column(name = "id_produto_externo")
    private Long idProduto;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    @Column(length = 255)
    private String descricao;

    @NotNull(message = "O preço de venda é obrigatório")
    @DecimalMin(value = "0.00", inclusive = false, message = "O preço de venda deve ser maior que zero")
    @Column(name = "preco_venda", precision = 10, scale = 2, nullable = false)
    private BigDecimal precoVenda;

    @Size(max = 255, message = "O link da imagem deve ter no máximo 255 caracteres")
    @Column(length = 255)
    private String imagem;

    @Size(max = 100, message = "A categoria deve ter no máximo 100 caracteres")
    @Column(length = 100)
    private String categoria;

    @Size(max = 50, message = "O código de barras deve ter no máximo 50 caracteres")
    @Column(name = "codigo_barras", unique = true, length = 50)
    private String codigoBarras;

    @DecimalMin(value = "0.00", inclusive = true, message = "O preço de custo não pode ser negativo")
    @Column(name = "preco_custo", precision = 10, scale = 2)
    private BigDecimal precoCusto;

    @Min(value = 0, message = "O estoque não pode ser negativo")
    @Column(nullable = false)
    private int estoque = 0;

    @NotNull(message = "A disponibilidade é obrigatória")
    @Column(nullable = false)
    private Boolean disponivel = Boolean.TRUE;

    @Size(max = 100, message = "O fabricante deve ter no máximo 100 caracteres")
    @Column(length = 100)
    private String fabricante;

    @Column(name = "codigo_original")
    private Long codigoOriginal;

    @Size(max = 20, message = "A unidade deve ter no máximo 20 caracteres")
    @Column(length = 20)
    private String unidade;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro = LocalDate.now();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProdutoEntity))
            return false;
        ProdutoEntity that = (ProdutoEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
