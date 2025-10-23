// src/test/java/br/com/redemaisfarma/unit/PedidoServiceTest.java
package br.com.redemaisfarma.unit;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.PedidoJPARepository; // <- JPA repo usado no service
import br.com.redemaisfarma.application.mapper.PedidoMapper;
import br.com.redemaisfarma.application.service.impl.PedidoServiceImpl;
import br.com.redemaisfarma.domain.Pedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoJPARepository repo;  // <- igual ao service

    @Mock
    private PedidoMapper mapper;

    @InjectMocks
    private PedidoServiceImpl service;

    @Test
    void deveRetornarPedidoPorId() {
        Long id = 1L;

        // repo -> entity
        PedidoEntity entity = new PedidoEntity();
        entity.setId(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        // mapper -> domain
        Pedido domain = new Pedido();
        domain.setId(id);
        when(mapper.toDomain(entity)).thenReturn(domain);

        // chamada
        Pedido result = service.findById(id);

        // asserts
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(repo).findById(id);
        verify(mapper).toDomain(entity);
        verifyNoMoreInteractions(repo, mapper);
    }
}
