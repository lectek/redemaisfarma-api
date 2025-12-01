// src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/repository/PedidoRepository.java
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    long countByStatus(StatusPedido status);

    // ====== Relatório • Clientes ======
    interface ClienteRelatorioRow {
        String getNome();
        Long getQtdPedidos();
        BigDecimal getValorTotal();
    }

    @Query("""
           select c.nome as nome,
                  count(p) as qtdPedidos,
                  coalesce(sum(coalesce(p.total, 0)), 0) as valorTotal
             from PedidoEntity p
             join p.cliente c
            group by c.id, c.nome
            order by coalesce(sum(coalesce(p.total, 0)), 0) desc
           """)
    List<ClienteRelatorioRow> listarResumoPorCliente();

    // ====== Relatório • Vendas por dia ======
    interface VendasRelatorioRowDia {
        LocalDate getData();
        Long getQtd();
        BigDecimal getTotal();
    }

    @Query("""
        select function('date', p.createdAt) as data,
               count(p) as qtd,
               coalesce(sum(coalesce(p.total, 0)), 0) as total
          from PedidoEntity p
         where (:de  is null or p.createdAt >= :de)
           and (:ate is null or p.createdAt <  :ate)
         group by function('date', p.createdAt)
         order by function('date', p.createdAt) asc
    """)
    List<VendasRelatorioRowDia> listarResumoVendasPorDia(LocalDateTime de, LocalDateTime ate);

    // ====== (Opcional) Relatório • Vendas por mês ======
    interface VendasRelatorioRowMes {
        Integer getAno();
        Integer getMes();
        Long getQtd();
        BigDecimal getTotal();
    }

    @Query("""
        select year(p.createdAt) as ano,
               month(p.createdAt) as mes,
               count(p) as qtd,
               coalesce(sum(coalesce(p.total, 0)), 0) as total
          from PedidoEntity p
         where (:de  is null or p.createdAt >= :de)
           and (:ate is null or p.createdAt <  :ate)
         group by year(p.createdAt), month(p.createdAt)
         order by year(p.createdAt), month(p.createdAt)
    """)
    List<VendasRelatorioRowMes> listarResumoVendasPorMes(LocalDateTime de, LocalDateTime ate);
}
