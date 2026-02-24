/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Repository
 */
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
    public Optional<ClienteEntity> findByEmail(String var1);

    public Optional<ClienteEntity> findByCpf(String var1);

    public boolean existsByEmail(String var1);

    public boolean existsByCpf(String var1);

    public boolean existsByEmailIgnoreCase(String var1);

    public Optional<ClienteEntity> findByEmailIgnoreCase(String var1);

    public Optional<ClienteEntity> findFirstByNomeContainingIgnoreCase(String var1);

    public long countByAtivoTrue();

    @Query("""
        select c
        from ClienteEntity c
        where c.ativo = true
          and coalesce(c.updatedAt, c.createdAt) < :cutoff
        """)
    List<ClienteEntity> findInativosAntesDe(@Param("cutoff") LocalDateTime cutoff);

    default public long countAtivos() {
        return this.countByAtivoTrue();
    }

    @Query("""
        select distinct p.cliente
        from PedidoEntity p
        join p.itens i
        join i.produto prod
        where lower(prod.categoria) = lower(:categoria)
          and p.status <> :cancelado
    """)
    List<ClienteEntity> findClientesByCategoriaComprada(@Param("categoria") String categoria,
                                                        @Param("cancelado") StatusPedido cancelado);

    @Query("""
        select distinct p.cliente
        from PedidoEntity p
        where p.data >= :from
          and p.status <> :cancelado
    """)
    List<ClienteEntity> findClientesByRecencia(@Param("from") LocalDateTime from,
                                                @Param("cancelado") StatusPedido cancelado);

    @Query("""
        select c
        from PedidoEntity p
        join p.cliente c
        where p.status <> :cancelado
        group by c
        having coalesce(avg(p.total), 0) >= :ticket
    """)
    List<ClienteEntity> findClientesByTicketMedio(@Param("ticket") BigDecimal ticket,
                                                  @Param("cancelado") StatusPedido cancelado);
}
