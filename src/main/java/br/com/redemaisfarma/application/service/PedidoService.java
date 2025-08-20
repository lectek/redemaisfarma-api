package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.redemaisfarma.domain.Pedido;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoJPARepository pedidoRepository;

    public PedidoService(PedidoJPARepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido salvar(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public void deletar(Long id) {
        pedidoRepository.deleteById(id);
    }
}
