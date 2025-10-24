/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.domain.enums.StatusPedido
 *  br.com.redemaisfarma.domain.enums.TipoPagamento
 *  jakarta.persistence.Access
 *  jakarta.persistence.AccessType
 *  jakarta.persistence.CascadeType
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EntityListeners
 *  jakarta.persistence.EnumType
 *  jakarta.persistence.Enumerated
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.OneToMany
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.Table
 *  jakarta.persistence.Version
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.DecimalMin$List
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.NotNull$List
 *  org.springframework.data.annotation.CreatedDate
 *  org.springframework.data.annotation.LastModifiedDate
 *  org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="pedido")
@Access(value=AccessType.FIELD)
@EntityListeners(value={AuditingEntityListener.class})
public class PedidoEntity
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="cliente_id", nullable=false)
    private ClienteEntity cliente;
    @NotNull
    @Column(nullable=false)
    private LocalDateTime data;
    @NotNull, @NotNull
    @DecimalMin(value="0.00", inclusive=true, message="Total n\u00e3o pode ser negativo")
@DecimalMin(value="0.00", inclusive=true, message="Total n\u00e3o pode ser negativo")
    @Column(precision=19, scale=2, nullable=false)
    private @NotNull, @NotNull @DecimalMin(value="0.00", inclusive=true, message="Total n\u00e3o pode ser negativo")
@DecimalMin(value="0.00", inclusive=true, message="Total n\u00e3o pode ser negativo") BigDecimal total;
    @OneToMany(mappedBy="pedido", cascade={CascadeType.ALL}, orphanRemoval=true)
    private List<ItemPedidoEntity> itens = new ArrayList<ItemPedidoEntity>();
    @NotNull
    @Enumerated(value=EnumType.STRING)
    @Column(nullable=false, length=30)
    private StatusPedido status;
    @NotNull
    @Enumerated(value=EnumType.STRING)
    @Column(name="tipo_pagamento", nullable=false, length=30)
    private TipoPagamento tipoPagamento;
    @CreatedDate
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    @Version
    @Column(nullable=false)
    private Long version;

    public void addItem(ItemPedidoEntity item) {
        if (item == null) {
            return;
        }
        this.itens.add(item);
        item.setPedido(this);
    }

    public void removeItem(ItemPedidoEntity item) {
        if (item == null) {
            return;
        }
        this.itens.remove(item);
        item.setPedido(null);
    }

    @PrePersist
    public void prePersist() {
        if (this.data == null) {
            this.data = LocalDateTime.now();
        }
        if (this.total == null) {
            this.total = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClienteEntity getCliente() {
        return this.cliente;
    }

    public void setCliente(ClienteEntity cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getData() {
        return this.data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<ItemPedidoEntity> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemPedidoEntity> itens) {
        this.itens.clear();
        if (itens != null) {
            itens.forEach(this::addItem);
        }
    }

    public StatusPedido getStatus() {
        return this.status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public TipoPagamento getTipoPagamento() {
        return this.tipoPagamento;
    }

    public void setTipoPagamento(TipoPagamento tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
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
        if (!(o instanceof PedidoEntity)) {
            return false;
        }
        PedidoEntity that = (PedidoEntity)o;
        return this.id != null && this.id.equals(that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "PedidoEntity{id=" + String.valueOf(this.id) + ", clienteId=" + String.valueOf(this.cliente != null ? this.cliente.getId() : null) + ", data=" + String.valueOf(this.data) + ", total=" + String.valueOf(this.total) + ", status=" + String.valueOf(this.status) + ", tipoPagamento=" + String.valueOf(this.tipoPagamento) + ", version=" + String.valueOf(this.version) + "}";
    }
}

