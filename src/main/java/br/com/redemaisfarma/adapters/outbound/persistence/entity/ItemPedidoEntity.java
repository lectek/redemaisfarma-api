/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EntityListeners
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.Table
 *  jakarta.persistence.Version
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.DecimalMin$List
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.Min$List
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.NotNull$List
 *  org.springframework.data.annotation.CreatedDate
 *  org.springframework.data.annotation.LastModifiedDate
 *  org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="item_pedido")
@EntityListeners(value={AuditingEntityListener.class})
public class ItemPedidoEntity
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="produto_id", nullable=false)
    private ProdutoEntity produto;
    @NotNull
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="pedido_id", nullable=false)
    private PedidoEntity pedido;
    @NotNull, @NotNull
    @Min(value=1L, message="Quantidade deve ser no m\u00ednimo 1")
@Min(value=1L, message="Quantidade deve ser no m\u00ednimo 1")
    @Column(nullable=false)
    private @NotNull, @NotNull @Min(value=1L, message="Quantidade deve ser no m\u00ednimo 1")
@Min(value=1L, message="Quantidade deve ser no m\u00ednimo 1") Integer quantidade;
    @NotNull, @NotNull
    @DecimalMin(value="0.00", inclusive=true, message="Subtotal n\u00e3o pode ser negativo")
@DecimalMin(value="0.00", inclusive=true, message="Subtotal n\u00e3o pode ser negativo")
    @Column(precision=19, scale=2, nullable=false)
    private @NotNull, @NotNull @DecimalMin(value="0.00", inclusive=true, message="Subtotal n\u00e3o pode ser negativo")
@DecimalMin(value="0.00", inclusive=true, message="Subtotal n\u00e3o pode ser negativo") BigDecimal subtotal;
    @CreatedDate
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    @Version
    @Column(nullable=false)
    private Long version;

    @PrePersist
    public void prePersist() {
        if (this.subtotal == null) {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProdutoEntity getProduto() {
        return this.produto;
    }

    public void setProduto(ProdutoEntity produto) {
        this.produto = produto;
    }

    public PedidoEntity getPedido() {
        return this.pedido;
    }

    public void setPedido(PedidoEntity pedido) {
        this.pedido = pedido;
    }

    public Integer getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getSubtotal() {
        return this.subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemPedidoEntity)) {
            return false;
        }
        ItemPedidoEntity that = (ItemPedidoEntity)o;
        return this.id != null && this.id.equals(that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "ItemPedidoEntity{id=" + String.valueOf(this.id) + ", produtoId=" + String.valueOf(this.produto != null ? this.produto.getId() : null) + ", pedidoId=" + String.valueOf(this.pedido != null ? this.pedido.getId() : null) + ", quantidade=" + String.valueOf(this.quantidade) + ", subtotal=" + String.valueOf(this.subtotal) + ", version=" + String.valueOf(this.version) + "}";
    }
}

