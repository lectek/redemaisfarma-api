package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pedido")
@Access(AccessType.FIELD) // <<< importante: mapeia pelos CAMPOS
@EntityListeners(AuditingEntityListener.class)
public class PedidoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime data;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true, message = "Total não pode ser negativo")
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedidoEntity> itens = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pagamento", nullable = false, length = 30)
    private TipoPagamento tipoPagamento;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public PedidoEntity() {
    }

    public void addItem(ItemPedidoEntity item) {
        if (item == null)
            return;
        itens.add(item);
        item.setPedido(this);
    }

    public void removeItem(ItemPedidoEntity item) {
        if (item == null)
            return;
        itens.remove(item);
        item.setPedido(null);
    }

    @PrePersist
    public void prePersist() {
        if (this.data == null)
            this.data = LocalDateTime.now();
        if (this.total == null)
            this.total = BigDecimal.ZERO;
    }

    // getters/setters SEM anotações JPA

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClienteEntity getCliente() {
        return cliente;
    }

    public void setCliente(ClienteEntity cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<ItemPedidoEntity> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoEntity> itens) {
        this.itens.clear();
        if (itens != null)
            itens.forEach(this::addItem);
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public TipoPagamento getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(TipoPagamento tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PedidoEntity that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PedidoEntity{" + "id=" + id + ", clienteId=" + (cliente != null ? cliente.getId() : null) + ", data="
                + data + ", total=" + total + ", status=" + status + ", tipoPagamento=" + tipoPagamento + ", version="
                + version + '}';
    }
}
