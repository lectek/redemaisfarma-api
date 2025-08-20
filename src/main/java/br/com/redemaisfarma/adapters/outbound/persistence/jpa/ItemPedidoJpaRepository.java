package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;

public interface ItemPedidoJpaRepository extends JpaRepository<ItemPedidoEntity, Long> {
    // Ex: List<ItemPedidoEntity> findByPedidoId(Long pedidoId);
}
