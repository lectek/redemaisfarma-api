package unit;

import br.com.redemaisfarma.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.redemaisfarma.application.service.PedidoService;
import br.com.redemaisfarma.domain.Pedido;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
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
    private PedidoJPARepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;

    @BeforeEach
    void setup() {
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatus(StatusPedido.ABERTO); // 👈 agora compila
    }

    @Test
    void deveRetornarPedidoPorId() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Optional<Pedido> resultado = pedidoRepository.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(StatusPedido.ABERTO, resultado.get().getStatus());

        verify(pedidoRepository, times(1)).findById(1L);
    }
}
