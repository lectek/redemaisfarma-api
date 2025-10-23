// src/main/java/br/com/redemaisfarma/application/service/impl/PedidoServiceImpl.java
package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.redemaisfarma.application.mapper.PedidoMapper;
import br.com.redemaisfarma.application.service.PedidoService;
import br.com.redemaisfarma.domain.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoJPARepository repo;   // JpaRepository<PedidoEntity, Long>
    private final PedidoMapper mapper;        // MapStruct (componentModel = "spring")

    @Override
    public Pedido findById(Long id) {
        PedidoEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));
        return mapper.toDomain(entity);
    }

    @Override
    public List<Pedido> list() {
        return repo.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional // write
    public Pedido create(Pedido pedido) {
        // Converte domínio -> entidade e deixa @PrePersist preencher defaults (data/total se nulos)
        PedidoEntity entity = mapper.toEntity(pedido);
        entity.setId(null); // garante INSERT
        PedidoEntity saved = repo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional // write
    public Pedido update(Long id, Pedido pedido) {
        // Carrega atual do banco
        PedidoEntity atual = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));

        // Mapeia domínio -> entidade "fonte" e aplica no atual
        PedidoEntity fonte = mapper.toEntity(pedido);

        // Campos simples
        atual.setCliente(fonte.getCliente());
        atual.setData(fonte.getData());
        atual.setTotal(fonte.getTotal());
        atual.setStatus(fonte.getStatus());
        atual.setTipoPagamento(fonte.getTipoPagamento());

        // Itens (usa o setter da entidade, que faz clear + addItem mantendo o vínculo bidirecional)
        if (fonte.getItens() != null) {
            atual.setItens(fonte.getItens());
        }

        PedidoEntity saved = repo.save(atual);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional // write
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Pedido não encontrado: " + id);
        }
        repo.deleteById(id);
    }
}
