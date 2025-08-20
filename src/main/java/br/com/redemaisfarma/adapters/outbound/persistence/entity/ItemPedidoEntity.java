package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Item de um Pedido. - Relações LAZY para performance - Precisão monetária com BigDecimal (DECIMAL 19,2) - Auditoria
 * (created_at/updated_at) e versionamento otimista - Validações de domínio mínimas (quantidade >= 1, subtotal >= 0)
 */
@Entity
@Table(name = "item_pedido")
@EntityListeners(AuditingEntityListener.class)
public class ItemPedidoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoEntity produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @NotNull
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    @Column(nullable = false)
    private Integer quantidade;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true, message = "Subtotal não pode ser negativo")
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public ItemPedidoEntity() {
    }

    /*
     * ======================= Callbacks JPA =======================
     */
    @PrePersist
    public void prePersist() {
        if (this.subtotal == null) {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /*
     * ======================= Getters/Setters =======================
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProdutoEntity getProduto() {
        return produto;
    }

    public void setProduto(ProdutoEntity produto) {
        this.produto = produto;
    }

    public PedidoEntity getPedido() {
        return pedido;
    }

    public void setPedido(PedidoEntity pedido) {
        this.pedido = pedido;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /*
     * ======================= equals / hashCode / toString =======================
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ItemPedidoEntity that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemPedidoEntity{" + "id=" + id + ", produtoId=" + (produto != null ? produto.getId() : null)
                + ", pedidoId=" + (pedido != null ? pedido.getId() : null) + ", quantidade=" + quantidade
                + ", subtotal=" + subtotal + ", version=" + version + '}';
    }
}
