/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.mapper.PedidoMapper
 *  br.com.redemaisfarma.domain.Pedido
 *  lombok.Generated
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.redemaisfarma.application.mapper.PedidoMapper;
import br.com.redemaisfarma.application.service.PedidoService;
import br.com.redemaisfarma.domain.Pedido;
import java.util.List;
import lombok.Generated;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class PedidoServiceImpl
implements PedidoService {
    private final PedidoJPARepository repo;
    private final PedidoMapper mapper;

    @Override
    public Pedido findById(Long id) {
        PedidoEntity entity = (PedidoEntity)this.repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido n\u00e3o encontrado: " + String.valueOf(id)));
        return this.mapper.toDomain(entity);
    }

    @Override
    public List<Pedido> list() {
        return this.repo.findAll().stream().map(arg_0 -> ((PedidoMapper)this.mapper).toDomain(arg_0)).toList();
    }

    @Override
    @Transactional
    public Pedido create(Pedido pedido) {
        PedidoEntity entity = this.mapper.toEntity(pedido);
        entity.setId(null);
        PedidoEntity saved = (PedidoEntity)this.repo.save(entity);
        return this.mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public Pedido update(Long id, Pedido pedido) {
        PedidoEntity atual = (PedidoEntity)this.repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido n\u00e3o encontrado: " + String.valueOf(id)));
        PedidoEntity fonte = this.mapper.toEntity(pedido);
        atual.setCliente(fonte.getCliente());
        atual.setData(fonte.getData());
        atual.setTotal(fonte.getTotal());
        atual.setStatus(fonte.getStatus());
        atual.setTipoPagamento(fonte.getTipoPagamento());
        if (fonte.getItens() != null) {
            atual.setItens(fonte.getItens());
        }
        PedidoEntity saved = (PedidoEntity)this.repo.save(atual);
        return this.mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!this.repo.existsById(id)) {
            throw new IllegalArgumentException("Pedido n\u00e3o encontrado: " + String.valueOf(id));
        }
        this.repo.deleteById(id);
    }

    @Generated
    public PedidoServiceImpl(PedidoJPARepository repo, PedidoMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }
}

