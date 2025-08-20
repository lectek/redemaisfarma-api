package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoJPARepository extends JpaRepository<Pedido, Long> {
}
