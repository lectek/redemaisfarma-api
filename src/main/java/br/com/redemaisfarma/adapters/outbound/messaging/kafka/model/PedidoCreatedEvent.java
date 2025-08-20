package br.com.redemaisfarma.adapters.outbound.messaging.kafka.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Exemplo de evento de domínio publicado quando um pedido é criado. */
public class PedidoCreatedEvent implements Serializable {
    private UUID pedidoId;
    private Long usuarioId;
    private BigDecimal valorTotal;
    private Instant criadoEm;

    public PedidoCreatedEvent() {
    }

    public PedidoCreatedEvent(UUID pedidoId, Long usuarioId, BigDecimal valorTotal, Instant criadoEm) {
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.valorTotal = valorTotal;
        this.criadoEm = criadoEm;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PedidoCreatedEvent that))
            return false;
        return Objects.equals(pedidoId, that.pedidoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pedidoId);
    }
}
